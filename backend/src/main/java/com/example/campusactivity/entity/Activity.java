package com.example.campusactivity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * 活动实体类
 * 存储校园活动的完整信息，包括发布时间、地点、状态等
 */
@Data
@TableName("activity")
@Schema(description = "活动信息")
public class Activity {

    /**
     * 活动ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "活动ID")
    private Long id;

    /**
     * 活动标题
     */
    @NotBlank(message = "活动标题不能为空")
    @Size(min = 2, max = 200, message = "活动标题长度需要2-200个字符")
    @Schema(description = "活动标题")
    private String title;

    /**
     * 活动详情描述
     */
    @NotBlank(message = "活动详情不能为空")
    @Size(min = 10, max = 2000, message = "活动详情长度需要10-2000个字符")
    @Schema(description = "活动详情")
    private String description;

    /**
     * 活动地点
     */
    @NotBlank(message = "活动地点不能为空")
    @Size(min = 2, max = 200, message = "活动地点长度需要2-200个字符")
    @Schema(description = "活动地点")
    private String location;

    /**
     * 活动开始时间
     */
    @NotNull(message = "活动开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "活动开始时间")
    private LocalDateTime startTime;

    /**
     * 活动结束时间
     */
    @NotNull(message = "活动结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "活动结束时间")
    private LocalDateTime endTime;

    /**
     * 报名开始时间
     */
    @NotNull(message = "报名开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "报名开始时间")
    private LocalDateTime registerStart;

    /**
     * 报名截止时间
     */
    @NotNull(message = "报名截止时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "报名截止时间")
    private LocalDateTime registerEnd;

    /**
     * 人数上限（0表示不限）
     */
    @NotNull(message = "人数上限不能为空")
    @Min(value = 0, message = "人数上限不能为负数")
    @Max(value = 100000, message = "人数上限不能超过100000")
    @Schema(description = "人数上限（0表示不限）")
    private Integer maxParticipants;

    /**
     * 当前报名人数
     */
    @Schema(description = "当前报名人数")
    private Integer currentParticipants;

    /**
     * 发布人ID（负责人）
     */
    @Schema(description = "发布人ID")
    private Long organizerId;

    /**
     * 发布人姓名
     */
    @Schema(description = "发布人姓名")
    private String organizerName;

    /**
     * 负责人联系方式
     */
    @Schema(description = "负责人联系方式")
    private String organizerContact;

    /**
     * 活动类型
     */
    @NotBlank(message = "活动类型不能为空")
    @Schema(description = "活动类型（学术/文体/志愿/社团/其他）")
    private String activityType;

    /**
     * 活动封面图URL
     */
    @Schema(description = "活动封面图")
    private String coverImage;

    /**
     * 活动状态（0-草稿，1-待审核，2-已发布，3-报名中，4-已结束，5-已取消）
     */
    @Schema(description = "活动状态（0-草稿，1-待审核，2-已发布，3-报名中，4-已结束，5-已取消）")
    private Integer status;

    /**
     * 审核驳回原因
     */
    @Schema(description = "驳回原因")
    private String rejectReason;

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

    // ==================== 非数据库字段 ====================

    /**
     * 当前用户是否已报名（查询时动态填充）
     */
    @TableField(exist = false)
    @Schema(description = "当前用户是否已报名")
    private Boolean hasRegistered;

    /**
     * 当前用户的报名状态（查询时动态填充）
     */
    @TableField(exist = false)
    @Schema(description = "当前用户报名状态")
    private Integer registrationStatus;

    /**
     * 报名状态中文描述
     */
    @TableField(exist = false)
    @Schema(description = "报名状态描述")
    private String statusText;

    public String getStatusText() {
        switch (this.status != null ? this.status : -1) {
            case 0: return "草稿";
            case 1: return "待审核";
            case 2: return "已发布";
            case 3: return "报名中";
            case 4: return "已结束";
            case 5: return "已取消";
            default: return "未知";
        }
    }
}
