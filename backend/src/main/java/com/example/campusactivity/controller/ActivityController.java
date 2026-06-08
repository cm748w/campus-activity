package com.example.campusactivity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campusactivity.entity.Activity;
import com.example.campusactivity.service.ActivityService;
import com.example.campusactivity.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 活动控制器
 * 处理活动相关的HTTP请求：发布、查询、审核、管理
 */
@RestController
@RequestMapping("/activity")
@Tag(name = "活动管理", description = "活动发布、查询、审核、管理接口")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    /**
     * 发布活动（负责人）
     * POST /api/activity/publish
     */
    @PostMapping("/publish")
    @Operation(summary = "发布活动", description = "负责人发布新活动，提交后等待管理员审核")
    public Result<Void> publishActivity(@Valid @RequestBody Activity activity) {
        return activityService.publishActivity(activity);
    }

    /**
     * 更新活动（负责人）
     * PUT /api/activity/{activityId}
     */
    @PutMapping("/{activityId}")
    @Operation(summary = "更新活动", description = "负责人更新自己发布的活动信息")
    public Result<Void> updateActivity(
            @Parameter(description = "活动ID") @PathVariable Long activityId,
            @Valid @RequestBody Activity activity) {
        activity.setId(activityId);
        return activityService.updateActivity(activity);
    }

    /**
     * 删除活动（负责人/管理员）
     * DELETE /api/activity/{activityId}
     */
    @DeleteMapping("/{activityId}")
    @Operation(summary = "删除活动", description = "删除活动（负责人只能删除自己的，管理员可删除所有）")
    public Result<Void> deleteActivity(
            @Parameter(description = "活动ID") @PathVariable Long activityId) {
        return activityService.deleteActivity(activityId);
    }

    /**
     * 分页查询活动列表（公开接口 - 学生查看已发布的活动）
     * GET /api/activity/list
     */
    @GetMapping("/list")
    @Operation(summary = "查询活动列表", description = "分页查询已发布的活动列表，支持关键词和类型筛选")
    public Result<IPage<Activity>> listActivities(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "活动类型") @RequestParam(required = false) String activityType,
            @Parameter(description = "活动状态") @RequestParam(required = false) Integer status) {
        return activityService.listActivities(pageNum, pageSize, keyword, activityType, status);
    }

    /**
     * 获取活动详情
     * GET /api/activity/{activityId}
     */
    @GetMapping("/{activityId}")
    @Operation(summary = "获取活动详情", description = "根据活动ID获取详细信息")
    public Result<Activity> getActivityDetail(
            @Parameter(description = "活动ID") @PathVariable Long activityId) {
        return activityService.getActivityDetail(activityId);
    }

    /**
     * 审核活动（管理员）
     * PUT /api/activity/audit/{activityId}
     */
    @PutMapping("/audit/{activityId}")
    @Operation(summary = "审核活动", description = "管理员审核活动发布申请（通过/驳回）")
    public Result<Void> auditActivity(
            @Parameter(description = "活动ID") @PathVariable Long activityId,
            @Parameter(description = "审核状态（2-通过，1-驳回）") @RequestParam Integer status,
            @Parameter(description = "驳回原因") @RequestParam(required = false) String rejectReason) {
        return activityService.auditActivity(activityId, status, rejectReason);
    }

    /**
     * 查询负责人发布的活动列表
     * GET /api/activity/organizer/list
     */
    @GetMapping("/organizer/list")
    @Operation(summary = "我的活动列表", description = "负责人查询自己发布的活动列表")
    public Result<IPage<Activity>> listOrganizerActivities(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "活动状态") @RequestParam(required = false) Integer status) {
        return activityService.listOrganizerActivities(pageNum, pageSize, keyword, status);
    }

    /**
     * 获取活动统计数据
     * GET /api/activity/stats/{activityId}
     */
    @GetMapping("/stats/{activityId}")
    @Operation(summary = "活动统计数据", description = "获取活动的报名人数、通过率等统计数据")
    public Result<Map<String, Object>> getActivityStats(
            @Parameter(description = "活动ID") @PathVariable Long activityId) {
        return activityService.getActivityStats(activityId);
    }

    /**
     * 获取活动类型列表
     * GET /api/activity/types
     */
    @GetMapping("/types")
    @Operation(summary = "活动类型列表", description = "获取所有活动类型选项")
    public Result<List<String>> listActivityTypes() {
        return activityService.listActivityTypes();
    }
}
