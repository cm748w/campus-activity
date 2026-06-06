package com.example.campusactivity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 报名实体类
 * 存储学生对活动的报名信息，包含审核状态
 */
@Data
@TableName("registration")
@Schema(description = "报名信息")
public class Registration {

    /**
     * 报名ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "报名ID")
    private Long id;

    /**
     * 活动ID
     */
    @Schema(description = "活动ID")
    private Long activityId;

    /**
     * 报名用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 报名人姓名
     */
    @Schema(description = "报名人姓名")
    private String realName;

    /**
     * 学号
     */
    @Schema(description = "学号")
    private String studentNo;

    /**
     * 联系电话
     */
    @Schema(description = "联系电话")
    private String phone;

    /**
     * 所在院系
     */
    @Schema(description = "所在院系")
    private String department;

    /**
     * 报名备注/自我介绍
     */
    @Schema(description = "报名备注")
    private String remark;

    /**
     * 报名状态（0-待审核，1-已通过，2-已驳回，3-已取消）
     */
    @Schema(description = "报名状态（0-待审核，1-已通过，2-已驳回，3-已取消）")
    private Integer status;

    /**
     * 审核意见/驳回原因
     */
    @Schema(description = "审核意见")
    private String rejectReason;

    /**
     * 报名时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "报名时间")
    private LocalDateTime registerTime;

    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

    /**
     * 审核人ID
     */
    @Schema(description = "审核人ID")
    private Long auditBy;

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
     * 关联活动信息
     */
    @TableField(exist = false)
    @Schema(description = "活动信息")
    private Activity activity;

    /**
     * 状态中文描述
     */
    @TableField(exist = false)
    @Schema(description = "状态描述")
    private String statusText;

    public String getStatusText() {
        switch (this.status != null ? this.status : -1) {
            case 0: return "待审核";
            case 1: return "已通过";
            case 2: return "已驳回";
            case 3: return "已取消";
            default: return "未知";
        }
    }
}
