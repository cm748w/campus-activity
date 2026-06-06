package com.example.campusactivity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campusactivity.entity.Activity;
import com.example.campusactivity.vo.Result;

import java.util.List;
import java.util.Map;

/**
 * 活动服务接口
 * 提供活动发布、审核、查询等业务逻辑
 */
public interface ActivityService extends IService<Activity> {

    /**
     * 发布活动（负责人）
     * @param activity 活动信息
     * @return 操作结果
     */
    Result<Void> publishActivity(Activity activity);

    /**
     * 更新活动
     * @param activity 活动信息
     * @return 操作结果
     */
    Result<Void> updateActivity(Activity activity);

    /**
     * 删除活动
     * @param activityId 活动ID
     * @return 操作结果
     */
    Result<Void> deleteActivity(Long activityId);

    /**
     * 分页查询活动列表（学生端 - 只看已发布的）
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keyword 关键词
     * @param activityType 活动类型
     * @param status 状态
     * @return 分页结果
     */
    Result<IPage<Activity>> listActivities(Integer pageNum, Integer pageSize, 
                                            String keyword, String activityType, Integer status);

    /**
     * 获取活动详情
     * @param activityId 活动ID
     * @return 活动详情
     */
    Result<Activity> getActivityDetail(Long activityId);

    /**
     * 审核活动（管理员）
     * @param activityId 活动ID
     * @param status 审核状态（2-通过，1-驳回）
     * @param rejectReason 驳回原因
     * @return 操作结果
     */
    Result<Void> auditActivity(Long activityId, Integer status, String rejectReason);

    /**
     * 查询负责人发布的活动列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keyword 关键词
     * @param status 状态
     * @return 分页结果
     */
    Result<IPage<Activity>> listOrganizerActivities(Integer pageNum, Integer pageSize, 
                                                     String keyword, Integer status);

    /**
     * 获取活动统计数据
     * @param activityId 活动ID
     * @return 统计数据
     */
    Result<Map<String, Object>> getActivityStats(Long activityId);

    /**
     * 获取活动类型列表
     * @return 类型列表
     */
    Result<List<String>> listActivityTypes();
}
