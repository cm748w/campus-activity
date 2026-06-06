package com.example.campusactivity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusactivity.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 活动数据访问层
 * 提供活动相关的数据库操作方法
 */
@Mapper
public interface ActivityMapper extends BaseMapper<Activity> {

    /**
     * 分页查询活动列表（支持关键词、类型、状态筛选）
     * @param page 分页参数
     * @param keyword 关键词搜索
     * @param activityType 活动类型筛选
     * @param status 状态筛选
     * @param userId 当前用户ID（用于判断是否已报名）
     * @return 分页结果
     */
    IPage<Activity> selectActivityPage(Page<Activity> page,
                                       @Param("keyword") String keyword,
                                       @Param("activityType") String activityType,
                                       @Param("status") Integer status,
                                       @Param("userId") Long userId);

    /**
     * 根据ID查询活动详情
     * @param id 活动ID
     * @param userId 当前用户ID
     * @return 活动详情
     */
    Activity selectActivityById(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 增加报名人数
     * @param activityId 活动ID
     * @return 影响行数
     */
    @Update("UPDATE activity SET current_participants = current_participants + 1 " +
            "WHERE id = #{activityId} AND (max_participants = 0 OR current_participants < max_participants)")
    int incrementParticipants(Long activityId);

    /**
     * 减少报名人数
     * @param activityId 活动ID
     * @return 影响行数
     */
    @Update("UPDATE activity SET current_participants = current_participants - 1 " +
            "WHERE id = #{activityId} AND current_participants > 0")
    int decrementParticipants(Long activityId);

    /**
     * 查询负责人发布的活动列表
     * @param page 分页参数
     * @param organizerId 负责人ID
     * @param keyword 关键词
     * @param status 状态筛选
     * @return 分页结果
     */
    IPage<Activity> selectOrganizerActivities(Page<Activity> page,
                                              @Param("organizerId") Long organizerId,
                                              @Param("keyword") String keyword,
                                              @Param("status") Integer status);

    /**
     * 查询活动统计信息
     * @param activityId 活动ID
     * @return 统计信息
     */
    @Select("SELECT " +
            "  COUNT(*) as totalCount, " +
            "  SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END) as approvedCount, " +
            "  SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END) as pendingCount, " +
            "  SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END) as rejectedCount " +
            "FROM registration WHERE activity_id = #{activityId} AND deleted = 0")
    List<java.util.Map<String, Object>> selectActivityStats(Long activityId);
}
