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
import com.example.campusactivity.service.ActivityService;
import com.example.campusactivity.utils.UserContext;
import com.example.campusactivity.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 活动服务实现类
 */
@Slf4j
@Service
public class ActivityServiceImpl extends ServiceImpl<ActivityMapper, Activity> implements ActivityService {

    @Autowired
    private ActivityMapper activityMapper;

    @Autowired
    private RegistrationMapper registrationMapper;

    /**
     * 发布活动
     */
    @Override
    public Result<Void> publishActivity(Activity activity) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }

        // 验证时间逻辑
        if (activity.getRegisterStart() == null || activity.getRegisterEnd() == null || 
            activity.getStartTime() == null || activity.getEndTime() == null) {
            throw new BusinessException("请填写所有时间字段");
        }
        
        if (!activity.getRegisterStart().isBefore(activity.getRegisterEnd())) {
            throw new BusinessException("报名开始时间必须早于报名结束时间");
        }
        
        if (!activity.getRegisterEnd().isBefore(activity.getStartTime()) && 
            !activity.getRegisterEnd().isEqual(activity.getStartTime())) {
            throw new BusinessException("报名结束时间不能晚于活动开始时间");
        }
        
        if (!activity.getStartTime().isBefore(activity.getEndTime())) {
            throw new BusinessException("活动开始时间必须早于结束时间");
        }

        // 设置发布人信息
        activity.setOrganizerId(currentUser.getId());
        activity.setOrganizerName(currentUser.getRealName());
        activity.setOrganizerContact(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        activity.setCurrentParticipants(0);
        
        // 默认状态为待审核（需要管理员审核后才能发布）
        activity.setStatus(1);

        // 保存活动
        save(activity);
        log.info("用户{}发布活动: {}", currentUser.getUsername(), activity.getTitle());
        return Result.success("活动发布成功，等待管理员审核");
    }

    /**
     * 更新活动
     */
    @Override
    public Result<Void> updateActivity(Activity activity) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }

        // 查询原活动
        Activity oldActivity = getById(activity.getId());
        if (oldActivity == null) {
            throw new BusinessException(404, "活动不存在");
        }

        // 只能修改自己发布的活动
        if (!oldActivity.getOrganizerId().equals(currentUser.getId())) {
            throw new BusinessException(403, "只能修改自己发布的活动");
        }

        // 已结束或已取消的活动不能修改
        if (oldActivity.getStatus() == 4 || oldActivity.getStatus() == 5) {
            return Result.error("已结束或已取消的活动不能修改");
        }

        // 检查新的人数限制是否小于已报名人数
        if (activity.getMaxParticipants() > 0 && activity.getMaxParticipants() < oldActivity.getCurrentParticipants()) {
            return Result.error("新的人数上限(" + activity.getMaxParticipants() + ")不能小于已报名人数(" + oldActivity.getCurrentParticipants() + ")");
        }

        // 更新后状态重置为待审核
        activity.setStatus(1);
        activity.setOrganizerId(null); // 不允许修改发布人
        activity.setOrganizerName(null);
        updateById(activity);

        log.info("用户{}更新活动: {}", currentUser.getUsername(), activity.getTitle());
        return Result.success("活动更新成功，等待管理员重新审核");
    }

    /**
     * 删除活动
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> deleteActivity(Long activityId) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }

        Activity activity = getById(activityId);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }

        // 只能删除自己发布的活动（管理员可以删除所有）
        boolean isAdmin = "admin".equals(currentUser.getRoleCode());
        if (!activity.getOrganizerId().equals(currentUser.getId()) && !isAdmin) {
            throw new BusinessException(403, "只能删除自己发布的活动");
        }

        // 删除活动（逻辑删除）
        removeById(activityId);
        log.info("用户{}删除活动: {}", currentUser.getUsername(), activity.getTitle());
        return Result.success("活动删除成功");
    }

    /**
     * 分页查询活动列表（学生端）
     */
    @Override
    public Result<IPage<Activity>> listActivities(Integer pageNum, Integer pageSize, 
                                                   String keyword, String activityType, Integer status) {
        User currentUser = UserContext.getUser();
        Long userId = currentUser != null ? currentUser.getId() : null;

        Page<Activity> page = new Page<>(pageNum, pageSize);
        IPage<Activity> activityPage = activityMapper.selectActivityPage(page, keyword, activityType, status, userId);

        // 填充状态文字描述
        activityPage.getRecords().forEach(a -> a.getStatusText());

        return Result.success(activityPage);
    }

    /**
     * 获取活动详情
     */
    @Override
    public Result<Activity> getActivityDetail(Long activityId) {
        User currentUser = UserContext.getUser();
        Long userId = currentUser != null ? currentUser.getId() : null;

        Activity activity = activityMapper.selectActivityById(activityId, userId);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }

        activity.getStatusText();
        return Result.success(activity);
    }

    /**
     * 审核活动（管理员）
     */
    @Override
    public Result<Void> auditActivity(Long activityId, Integer status, String rejectReason) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }

        // 检查是否为管理员
        if (!"admin".equals(currentUser.getRoleCode())) {
            throw new BusinessException(403, "只有管理员可以审核活动");
        }

        Activity activity = getById(activityId);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }

        if (status == 2) {
            // 审核通过 - 状态改为已发布
            activity.setStatus(2);
            activity.setRejectReason(null);
        } else {
            // 驳回 - 回到草稿状态，允许组织者修改后重新提交
            activity.setStatus(0);
            activity.setRejectReason(rejectReason);
        }

        updateById(activity);
        log.info("管理员{}审核活动{}: 状态={}", currentUser.getUsername(), activityId, status);
        return Result.success(status == 2 ? "审核通过" : "已驳回");
    }

    /**
     * 查询负责人发布的活动列表
     */
    @Override
    public Result<IPage<Activity>> listOrganizerActivities(Integer pageNum, Integer pageSize, 
                                                            String keyword, Integer status) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }

        Page<Activity> page = new Page<>(pageNum, pageSize);
        IPage<Activity> activityPage = activityMapper.selectOrganizerActivities(
                page, currentUser.getId(), keyword, status);

        activityPage.getRecords().forEach(a -> a.getStatusText());
        return Result.success(activityPage);
    }

    /**
     * 获取活动统计数据
     */
    @Override
    public Result<Map<String, Object>> getActivityStats(Long activityId) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }

        Activity activity = getById(activityId);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }

        // 只能查看自己发布的活动统计
        if (!activity.getOrganizerId().equals(currentUser.getId())) {
            throw new BusinessException(403, "只能查看自己发布的活动统计");
        }

        // 查询统计信息
        List<Map<String, Object>> stats = activityMapper.selectActivityStats(activityId);
        Map<String, Object> result = new HashMap<>();
        
        if (!stats.isEmpty()) {
            Map<String, Object> stat = stats.get(0);
            Object approvedCountObj = stat.get("approvedCount") != null ? stat.get("approvedCount") : 0;
            Object totalCountObj = stat.get("totalCount") != null ? stat.get("totalCount") : 0;
            
            result.put("totalCount", totalCountObj);
            result.put("approvedCount", approvedCountObj);
            result.put("pendingCount", stat.get("pendingCount") != null ? stat.get("pendingCount") : 0);
            result.put("rejectedCount", stat.get("rejectedCount") != null ? stat.get("rejectedCount") : 0);
            
            long approved = Long.parseLong(approvedCountObj.toString());
            long total = Long.parseLong(totalCountObj.toString());
            result.put("approvalRate", total > 0 ? String.format("%.1f%%", (double) approved / total * 100) : "0%");
        } else {
            result.put("totalCount", 0);
            result.put("approvedCount", 0);
            result.put("pendingCount", 0);
            result.put("rejectedCount", 0);
            result.put("approvalRate", "0%");
        }

        result.put("maxParticipants", activity.getMaxParticipants());
        result.put("currentParticipants", activity.getCurrentParticipants());

        return Result.success(result);
    }

    /**
     * 获取活动类型列表
     */
    @Override
    public Result<List<String>> listActivityTypes() {
        List<String> types = Arrays.asList("学术", "文体", "志愿", "社团", "其他");
        return Result.success(types);
    }
}
