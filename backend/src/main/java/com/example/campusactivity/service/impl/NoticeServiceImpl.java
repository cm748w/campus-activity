package com.example.campusactivity.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusactivity.entity.Notice;
import com.example.campusactivity.entity.User;
import com.example.campusactivity.exception.BusinessException;
import com.example.campusactivity.mapper.NoticeMapper;
import com.example.campusactivity.service.NoticeService;
import com.example.campusactivity.utils.UserContext;
import com.example.campusactivity.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 公告服务实现类
 */
@Slf4j
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    /**
     * 发布公告
     */
    @Override
    public Result<Void> publishNotice(Notice notice) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }

        // 检查是否为管理员
        if (!"admin".equals(currentUser.getRoleCode())) {
            throw new BusinessException(403, "只有管理员可以发布公告");
        }

        notice.setPublisherId(currentUser.getId());
        notice.setPublisherName(currentUser.getRealName());
        notice.setStatus(1); // 已发布
        notice.setViewCount(0);
        notice.setPublishTime(LocalDateTime.now());

        save(notice);
        log.info("管理员{}发布公告: {}", currentUser.getUsername(), notice.getTitle());
        return Result.success("公告发布成功");
    }

    /**
     * 更新公告
     */
    @Override
    public Result<Void> updateNotice(Notice notice) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }

        if (!"admin".equals(currentUser.getRoleCode())) {
            throw new BusinessException(403, "只有管理员可以更新公告");
        }

        Notice oldNotice = getById(notice.getId());
        if (oldNotice == null) {
            throw new BusinessException(404, "公告不存在");
        }

        // 不允许修改发布人
        notice.setPublisherId(null);
        notice.setPublisherName(null);
        notice.setCreateTime(null);

        updateById(notice);
        log.info("管理员{}更新公告: {}", currentUser.getUsername(), notice.getTitle());
        return Result.success("公告更新成功");
    }

    /**
     * 删除公告
     */
    @Override
    public Result<Void> deleteNotice(Long noticeId) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }

        if (!"admin".equals(currentUser.getRoleCode())) {
            throw new BusinessException(403, "只有管理员可以删除公告");
        }

        Notice notice = getById(noticeId);
        if (notice == null) {
            throw new BusinessException(404, "公告不存在");
        }

        removeById(noticeId);
        log.info("管理员{}删除公告: {}", currentUser.getUsername(), notice.getTitle());
        return Result.success("公告删除成功");
    }

    /**
     * 获取公告详情
     */
    @Override
    public Result<Notice> getNoticeDetail(Long noticeId) {
        Notice notice = getById(noticeId);
        if (notice == null) {
            throw new BusinessException(404, "公告不存在");
        }

        // 增加浏览次数
        noticeMapper.incrementViewCount(noticeId);
        notice.setViewCount(notice.getViewCount() + 1);

        return Result.success(notice);
    }

    /**
     * 分页查询公告列表
     */
    @Override
    public Result<IPage<Notice>> listNotices(Integer pageNum, Integer pageSize, String keyword, Integer status) {
        Page<Notice> page = new Page<>(pageNum, pageSize);
        IPage<Notice> noticePage = noticeMapper.selectNoticePage(page, keyword, status);
        return Result.success(noticePage);
    }

    /**
     * 查询最新公告
     */
    @Override
    public Result<IPage<Notice>> listLatestNotices(Integer limit) {
        Page<Notice> page = new Page<>(1, limit);
        IPage<Notice> noticePage = noticeMapper.selectLatestNotices(page);
        return Result.success(noticePage);
    }
}
