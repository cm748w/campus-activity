package com.example.campusactivity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campusactivity.entity.Notice;
import com.example.campusactivity.vo.Result;

/**
 * 公告服务接口
 * 提供公告发布、查询、管理等业务逻辑
 */
public interface NoticeService extends IService<Notice> {

    /**
     * 发布公告（管理员）
     * @param notice 公告信息
     * @return 操作结果
     */
    Result<Void> publishNotice(Notice notice);

    /**
     * 更新公告
     * @param notice 公告信息
     * @return 操作结果
     */
    Result<Void> updateNotice(Notice notice);

    /**
     * 删除公告
     * @param noticeId 公告ID
     * @return 操作结果
     */
    Result<Void> deleteNotice(Long noticeId);

    /**
     * 获取公告详情
     * @param noticeId 公告ID
     * @return 公告详情
     */
    Result<Notice> getNoticeDetail(Long noticeId);

    /**
     * 分页查询公告列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keyword 关键词
     * @param status 状态筛选
     * @return 分页结果
     */
    Result<IPage<Notice>> listNotices(Integer pageNum, Integer pageSize, String keyword, Integer status);

    /**
     * 查询最新公告（首页展示）
     * @param limit 条数
     * @return 公告列表
     */
    Result<IPage<Notice>> listLatestNotices(Integer limit);
}
