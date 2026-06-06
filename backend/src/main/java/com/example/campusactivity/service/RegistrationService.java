package com.example.campusactivity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campusactivity.entity.Registration;
import com.example.campusactivity.vo.Result;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

/**
 * 报名服务接口
 * 提供在线报名、审核、导出等业务逻辑
 */
public interface RegistrationService extends IService<Registration> {

    /**
     * 报名活动
     * @param activityId 活动ID
     * @param remark 报名备注
     * @return 操作结果
     */
    Result<Void> registerActivity(Long activityId, String remark);

    /**
     * 取消报名
     * @param registrationId 报名ID
     * @return 操作结果
     */
    Result<Void> cancelRegistration(Long registrationId);

    /**
     * 审核报名（负责人）
     * @param registrationId 报名ID
     * @param status 审核状态（1-通过，2-驳回）
     * @param rejectReason 驳回原因
     * @return 操作结果
     */
    Result<Void> auditRegistration(Long registrationId, Integer status, String rejectReason);

    /**
     * 查询活动的报名列表（负责人）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param activityId 活动ID
     * @param status 报名状态
     * @param keyword 关键词
     * @return 分页结果
     */
    Result<IPage<Registration>> listRegistrations(Integer pageNum, Integer pageSize, 
                                                   Long activityId, Integer status, String keyword);

    /**
     * 查询我的报名列表（学生）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param status 状态筛选
     * @return 分页结果
     */
    Result<IPage<Registration>> listMyRegistrations(Integer pageNum, Integer pageSize, Integer status);

    /**
     * 导出报名成功名单（Excel）
     * @param activityId 活动ID
     * @param response HTTP响应
     */
    void exportApprovedList(Long activityId, HttpServletResponse response);
}
