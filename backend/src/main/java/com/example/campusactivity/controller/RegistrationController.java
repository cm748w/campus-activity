package com.example.campusactivity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campusactivity.entity.Registration;
import com.example.campusactivity.service.RegistrationService;
import com.example.campusactivity.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 报名控制器
 * 处理报名相关的HTTP请求：在线报名、取消报名、审核、导出
 */
@RestController
@RequestMapping("/registration")
@Tag(name = "报名管理", description = "在线报名、审核、导出名单接口")
public class RegistrationController {

    @Autowired
    private RegistrationService registrationService;

    /**
     * 报名活动（学生）
     * POST /api/registration/apply/{activityId}
     */
    @PostMapping("/apply/{activityId}")
    @Operation(summary = "报名活动", description = "学生报名参加活动，提交后等待负责人审核")
    public Result<Void> registerActivity(
            @Parameter(description = "活动ID") @PathVariable Long activityId,
            @Parameter(description = "报名备注") @RequestParam(required = false) String remark) {
        return registrationService.registerActivity(activityId, remark);
    }

    /**
     * 取消报名（学生）
     * PUT /api/registration/cancel/{registrationId}
     */
    @PutMapping("/cancel/{registrationId}")
    @Operation(summary = "取消报名", description = "学生在报名截止前取消报名")
    public Result<Void> cancelRegistration(
            @Parameter(description = "报名ID") @PathVariable Long registrationId) {
        return registrationService.cancelRegistration(registrationId);
    }

    /**
     * 审核报名（负责人）
     * PUT /api/registration/audit/{registrationId}
     */
    @PutMapping("/audit/{registrationId}")
    @Operation(summary = "审核报名", description = "负责人审核报名申请（通过/驳回）")
    public Result<Void> auditRegistration(
            @Parameter(description = "报名ID") @PathVariable Long registrationId,
            @Parameter(description = "审核状态（1-通过，2-驳回）") @RequestParam Integer status,
            @Parameter(description = "驳回原因") @RequestParam(required = false) String rejectReason) {
        return registrationService.auditRegistration(registrationId, status, rejectReason);
    }

    /**
     * 查询活动的报名列表（负责人）
     * GET /api/registration/list/{activityId}
     */
    @GetMapping("/list/{activityId}")
    @Operation(summary = "查询活动报名列表", description = "负责人查询自己发布的活动的所有报名记录")
    public Result<IPage<Registration>> listRegistrations(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "活动ID") @PathVariable Long activityId,
            @Parameter(description = "报名状态筛选") @RequestParam(required = false) Integer status,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return registrationService.listRegistrations(pageNum, pageSize, activityId, status, keyword);
    }

    /**
     * 查询我的报名列表（学生）
     * GET /api/registration/my
     */
    @GetMapping("/my")
    @Operation(summary = "我的报名列表", description = "学生查询自己的所有报名记录")
    public Result<IPage<Registration>> listMyRegistrations(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status) {
        return registrationService.listMyRegistrations(pageNum, pageSize, status);
    }

    /**
     * 导出报名成功名单（负责人）
     * GET /api/registration/export/{activityId}
     */
    @GetMapping("/export/{activityId}")
    @Operation(summary = "导出报名名单", description = "负责人导出报名成功的学生名单为CSV文件")
    public void exportApprovedList(
            @Parameter(description = "活动ID") @PathVariable Long activityId,
            HttpServletResponse response) {
        registrationService.exportApprovedList(activityId, response);
    }
}
