package com.example.campusactivity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.campusactivity.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 公告数据访问层
 * 提供公告相关的数据库操作方法
 */
@Mapper
public interface NoticeMapper extends BaseMapper<Notice> {

    /**
     * 分页查询公告列表（支持关键词、状态筛选）
     * @param page 分页参数
     * @param keyword 关键词搜索
     * @param status 状态筛选
     * @return 分页结果
     */
    IPage<Notice> selectNoticePage(Page<Notice> page,
                                   @Param("keyword") String keyword,
                                   @Param("status") Integer status);

    /**
     * 增加浏览次数
     * @param noticeId 公告ID
     */
    @Update("UPDATE notice SET view_count = view_count + 1 WHERE id = #{noticeId}")
    void incrementViewCount(Long noticeId);

    /**
     * 查询最新公告列表（首页展示用）
     * @param limit 查询条数
     * @return 公告列表
     */
    IPage<Notice> selectLatestNotices(Page<Notice> page);
}
