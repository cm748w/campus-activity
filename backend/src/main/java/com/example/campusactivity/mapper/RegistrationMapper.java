package com.example.campusactivity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusactivity.entity.Registration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 报名数据访问层
 * 提供报名相关的数据库操作方法
 */
@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {

    /**
     * 分页查询活动的报名列表
     * @param page 分页参数
     * @param activityId 活动ID
     * @param status 报名状态筛选
     * @param keyword 关键词搜索
     * @return 分页结果
     */
    IPage<Registration> selectRegistrationPage(Page<Registration> page,
                                               @Param("activityId") Long activityId,
                                               @Param("status") Integer status,
                                               @Param("keyword") String keyword);

    /**
     * 查询用户的报名列表（带活动信息）
     * @param page 分页参数
     * @param userId 用户ID
     * @param status 状态筛选
     * @return 分页结果
     */
    IPage<Registration> selectUserRegistrations(Page<Registration> page,
                                                @Param("userId") Long userId,
                                                @Param("status") Integer status);

    /**
     * 根据活动ID和用户ID查询报名记录
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return 报名记录
     */
    Registration selectByActivityAndUser(@Param("activityId") Long activityId, 
                                          @Param("userId") Long userId);

    /**
     * 审核报名
     * @param registrationId 报名ID
     * @param status 审核状态
     * @param rejectReason 驳回原因
     * @param auditBy 审核人ID
     */
    @Update("UPDATE registration SET status = #{status}, reject_reason = #{rejectReason}, " +
            "audit_time = NOW(), audit_by = #{auditBy} WHERE id = #{registrationId}")
    void auditRegistration(@Param("registrationId") Long registrationId,
                          @Param("status") Integer status,
                          @Param("rejectReason") String rejectReason,
                          @Param("auditBy") Long auditBy);

    /**
     * 导出报名成功名单
     * @param activityId 活动ID
     * @return 报名成功列表
     */
    List<Registration> selectApprovedList(@Param("activityId") Long activityId);
}
