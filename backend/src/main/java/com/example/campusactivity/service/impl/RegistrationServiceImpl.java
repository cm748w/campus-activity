package com.example.campusactivity.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusactivity.entity.Activity;
import com.example.campusactivity.entity.Registration;
import com.example.campusactivity.entity.User;
import com.example.campusactivity.exception.BusinessException;
import com.example.campusactivity.mapper.ActivityMapper;
import com.example.campusactivity.mapper.RegistrationMapper;
import com.example.campusactivity.service.RegistrationService;
import com.example.campusactivity.utils.UserContext;
import com.example.campusactivity.vo.Result;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报名服务实现类
 */
@Slf4j
@Service
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration> implements RegistrationService {

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private ActivityMapper activityMapper;

    /**
     * 报名活动
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> registerActivity(Long activityId, String remark) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }

        // 查询活动
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }

        // 检查活动状态
        if (activity.getStatus() != 2 && activity.getStatus() != 3) {
            return Result.error("该活动当前不可报名");
        }

        // 检查报名时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getRegisterStart())) {
            return Result.error("报名尚未开始");
        }
        if (now.isAfter(activity.getRegisterEnd())) {
            return Result.error("报名已截止");
        }

        // 检查人数上限
        if (activity.getMaxParticipants() > 0 && 
            activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
            return Result.error("该活动报名人数已满");
        }

        // 检查是否已报名
        Registration existReg = registrationMapper.selectByActivityAndUser(activityId, currentUser.getId());
        if (existReg != null) {
            if (existReg.getStatus() == 0) {
                return Result.error("您已提交报名，正在审核中");
            } else if (existReg.getStatus() == 1) {
                return Result.error("您已通过报名审核");
            } else if (existReg.getStatus() == 3) {
                // 已取消的报名可以重新报名
                removeById(existReg.getId());
            } else {
                return Result.error("您已报名该活动（状态：" + existReg.getStatusText() + "）");
            }
        }

        // 创建报名记录
        Registration registration = new Registration();
        registration.setActivityId(activityId);
        registration.setUserId(currentUser.getId());
        registration.setRealName(currentUser.getRealName());
        registration.setStudentNo(currentUser.getUsername());
        registration.setPhone(currentUser.getPhone());
        registration.setDepartment(currentUser.getDepartment());
        registration.setRemark(remark);
        registration.setStatus(0); // 待审核

        save(registration);

        // 增加报名人数（乐观锁）
        int result = activityMapper.incrementParticipants(activityId);
        if (result == 0) {
            throw new BusinessException("报名失败，人数已满");
        }

        log.info("用户{}报名活动: {}", currentUser.getUsername(), activity.getTitle());
        return Result.success("报名提交成功，等待审核");
    }

    /**
     * 取消报名
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancelRegistration(Long registrationId) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }

        Registration registration = getById(registrationId);
        if (registration == null) {
            throw new BusinessException(404, "报名记录不存在");
        }

        // 只能取消自己的报名
        if (!registration.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(403, "只能取消自己的报名");
        }

        // 已驳回的报名不能取消
        if (registration.getStatus() == 2) {
            return Result.error("已驳回的报名无需取消");
        }

        // 更新报名状态为已取消
        registration.setStatus(3);
        updateById(registration);

        // 减少报名人数
        activityMapper.decrementParticipants(registration.getActivityId());

        log.info("用户{}取消报名: {}", currentUser.getUsername(), registrationId);
        return Result.success("报名已取消");
    }

    /**
     * 审核报名（负责人）
     */
    @Override
    public Result<Void> auditRegistration(Long registrationId, Integer status, String rejectReason) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }

        Registration registration = getById(registrationId);
        if (registration == null) {
            throw new BusinessException(404, "报名记录不存在");
        }

        // 查询活动确认权限
        Activity activity = activityMapper.selectById(registration.getActivityId());
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }

        // 只能审核自己发布的活动的报名
        if (!activity.getOrganizerId().equals(currentUser.getId())) {
            throw new BusinessException(403, "只能审核自己发布的活动的报名");
        }

        // 执行审核
        registrationMapper.auditRegistration(registrationId, status, rejectReason, currentUser.getId());

        String action = status == 1 ? "通过" : "驳回";
        log.info("负责人{}审核报名{}: {}", currentUser.getUsername(), registrationId, action);
        return Result.success("审核" + action + "成功");
    }

    /**
     * 查询活动的报名列表
     */
    @Override
    public Result<IPage<Registration>> listRegistrations(Integer pageNum, Integer pageSize, 
                                                          Long activityId, Integer status, String keyword) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }

        // 验证活动归属
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }

        if (!activity.getOrganizerId().equals(currentUser.getId()) && !"admin".equals(currentUser.getRoleCode())) {
            throw new BusinessException(403, "只能查看自己发布的活动的报名");
        }

        Page<Registration> page = new Page<>(pageNum, pageSize);
        IPage<Registration> regPage = registrationMapper.selectRegistrationPage(page, activityId, status, keyword);
        regPage.getRecords().forEach(r -> r.getStatusText());

        return Result.success(regPage);
    }

    /**
     * 查询我的报名列表
     */
    @Override
    public Result<IPage<Registration>> listMyRegistrations(Integer pageNum, Integer pageSize, Integer status) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }

        Page<Registration> page = new Page<>(pageNum, pageSize);
        IPage<Registration> regPage = registrationMapper.selectUserRegistrations(page, currentUser.getId(), status);
        regPage.getRecords().forEach(r -> r.getStatusText());

        return Result.success(regPage);
    }

    /**
     * 导出报名成功名单（CSV格式）
     */
    @Override
    public void exportApprovedList(Long activityId, HttpServletResponse response) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }

        // 验证活动归属
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }

        if (!activity.getOrganizerId().equals(currentUser.getId())) {
            throw new BusinessException(403, "只能导出自己的活动名单");
        }

        // 查询报名成功的名单
        List<Registration> list = registrationMapper.selectApprovedList(activityId);

        // 设置响应头
        String fileName = URLEncoder.encode(activity.getTitle() + "-报名名单.csv", StandardCharsets.UTF_8);
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

        // 写入CSV数据
        try (ServletOutputStream out = response.getOutputStream()) {
            // UTF-8 BOM（解决Excel打开乱码）
            out.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            // 表头
            StringBuilder sb = new StringBuilder();
            sb.append("序号,姓名,学号,联系电话,院系,报名时间,备注\n");

            // 数据行
            for (int i = 0; i < list.size(); i++) {
                Registration r = list.get(i);
                sb.append(i + 1).append(",")
                  .append(escapeCsv(r.getRealName())).append(",")
                  .append(escapeCsv(r.getStudentNo())).append(",")
                  .append(escapeCsv(r.getPhone())).append(",")
                  .append(escapeCsv(r.getDepartment())).append(",")
                  .append(r.getRegisterTime()).append(",")
                  .append(escapeCsv(r.getRemark())).append("\n");
            }

            out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();

            log.info("负责人{}导出活动{}的报名名单，共{}人", currentUser.getUsername(), activity.getTitle(), list.size());
        } catch (IOException e) {
            log.error("导出报名名单失败", e);
            throw new BusinessException("导出失败");
        }
    }

    /**
     * CSV字段转义（处理包含逗号或引号的情况）
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
