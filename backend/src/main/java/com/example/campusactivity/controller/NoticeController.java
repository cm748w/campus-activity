package com.example.campusactivity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campusactivity.entity.Notice;
import com.example.campusactivity.service.NoticeService;
import com.example.campusactivity.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 公告控制器
 * 处理公告相关的HTTP请求：发布、查询、管理
 */
@RestController
@RequestMapping("/notice")
@Tag(name = "公告管理", description = "公告发布、查询、管理接口")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 发布公告（管理员）
     * POST /api/notice/publish
     */
    @PostMapping("/publish")
    @Operation(summary = "发布公告", description = "管理员发布新公告")
    public Result<Void> publishNotice(@RequestBody Notice notice) {
        return noticeService.publishNotice(notice);
    }

    /**
     * 更新公告（管理员）
     * PUT /api/notice/{noticeId}
     */
    @PutMapping("/{noticeId}")
    @Operation(summary = "更新公告", description = "管理员更新公告内容")
    public Result<Void> updateNotice(
            @Parameter(description = "公告ID") @PathVariable Long noticeId,
            @RequestBody Notice notice) {
        notice.setId(noticeId);
        return noticeService.updateNotice(notice);
    }

    /**
     * 删除公告（管理员）
     * DELETE /api/notice/{noticeId}
     */
    @DeleteMapping("/{noticeId}")
    @Operation(summary = "删除公告", description = "管理员删除公告")
    public Result<Void> deleteNotice(
            @Parameter(description = "公告ID") @PathVariable Long noticeId) {
        return noticeService.deleteNotice(noticeId);
    }

    /**
     * 获取公告详情
     * GET /api/notice/{noticeId}
     */
    @GetMapping("/{noticeId}")
    @Operation(summary = "获取公告详情", description = "根据公告ID获取详细信息，同时增加浏览次数")
    public Result<Notice> getNoticeDetail(
            @Parameter(description = "公告ID") @PathVariable Long noticeId) {
        return noticeService.getNoticeDetail(noticeId);
    }

    /**
     * 分页查询公告列表
     * GET /api/notice/list
     */
    @GetMapping("/list")
    @Operation(summary = "查询公告列表", description = "分页查询公告列表，支持关键词和状态筛选")
    public Result<IPage<Notice>> listNotices(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态筛选") @RequestParam(required = false) Integer status) {
        return noticeService.listNotices(pageNum, pageSize, keyword, status);
    }

    /**
     * 查询最新公告（首页展示用）
     * GET /api/notice/latest
     */
    @GetMapping("/latest")
    @Operation(summary = "最新公告", description = "查询最新的几条已发布公告，用于首页展示")
    public Result<IPage<Notice>> listLatestNotices(
            @Parameter(description = "查询条数") @RequestParam(defaultValue = "5") Integer limit) {
        return noticeService.listLatestNotices(limit);
    }
}
