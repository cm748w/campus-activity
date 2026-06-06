package com.example.campusactivity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体类
 * 系统三类用户的基础信息：管理员、负责人、学生
 */
@Data
@TableName("user")
@Schema(description = "用户信息")
public class User {

    /**
     * 用户ID
     */
    @TableId(type = IdType.AUTO)
    @Schema(description = "用户ID")
    private Long id;

    /**
     * 用户名/学号/工号
     */
    @Schema(description = "用户名/学号/工号")
    private String username;

    /**
     * 加密后的密码
     * JsonIgnore防止密码序列化到JSON返回给前端
     */
    @JsonIgnore
    @Schema(description = "密码（加密存储）")
    private String password;

    /**
     * 真实姓名
     */
    @Schema(description = "真实姓名")
    private String realName;

    /**
     * 手机号码
     */
    @Schema(description = "手机号码")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL")
    private String avatar;

    /**
     * 性别（0-保密，1-男，2-女）
     */
    @Schema(description = "性别（0-保密，1-男，2-女）")
    private Integer gender;

    /**
     * 所属院系/部门
     */
    @Schema(description = "所属院系/部门")
    private String department;

    /**
     * 账号状态（0-禁用，1-正常）
     */
    @Schema(description = "账号状态（0-禁用，1-正常）")
    private Integer status;

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
     * 用户角色列表（查询时关联填充）
     */
    @TableField(exist = false)
    @Schema(description = "角色列表")
    private List<Role> roles;

    /**
     * 角色编码字符串（用于前端判断权限）
     */
    @TableField(exist = false)
    @Schema(description = "角色编码")
    private String roleCode;

    /**
     * 角色名称字符串
     */
    @TableField(exist = false)
    @Schema(description = "角色名称")
    private String roleName;
}
