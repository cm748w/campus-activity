package com.example.campusactivity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告实体类
 * 存储系统公告信息，支持置顶功能
 */
@Data
@TableName("notice")
@Schema(description = "公告信息")
public class Notice {

    /**
     * 公告ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "公告ID")
    private Long id;

    /**
     * 公告标题
     */
    @Schema(description = "公告标题")
    private String title;

    /**
     * 公告内容
     */
    @Schema(description = "公告内容")
    private String content;

    /**
     * 发布人ID
     */
    @Schema(description = "发布人ID")
    private Long publisherId;

    /**
     * 发布人姓名
     */
    @Schema(description = "发布人姓名")
    private String publisherName;

    /**
     * 是否置顶（0-否，1-是）
     */
    @Schema(description = "是否置顶")
    private Integer top;

    /**
     * 公告状态（0-草稿，1-已发布，2-已下架）
     */
    @Schema(description = "公告状态（0-草稿，1-已发布，2-已下架）")
    private Integer status;

    /**
     * 浏览次数
     */
    @Schema(description = "浏览次数")
    private Integer viewCount;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "发布时间")
    private LocalDateTime publishTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标志
     */
    @TableLogic
    @Schema(description = "逻辑删除标志")
    private Integer deleted;
}
