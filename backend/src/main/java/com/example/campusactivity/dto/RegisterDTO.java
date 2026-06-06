package com.example.campusactivity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求DTO
 * 接收前端注册表单数据并进行参数校验
 */
@Data
@Schema(description = "用户注册请求")
public class RegisterDTO {

    /**
     * 用户名/学号
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Schema(description = "用户名/学号", required = true)
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    @Schema(description = "密码", required = true)
    private String password;

    /**
     * 确认密码
     */
    @NotBlank(message = "确认密码不能为空")
    @Schema(description = "确认密码", required = true)
    private String confirmPassword;

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    @Schema(description = "真实姓名", required = true)
    private String realName;

    /**
     * 手机号码
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号码")
    private String phone;

    /**
     * 邮箱
     */
    @Schema(description = "邮箱")
    private String email;

    /**
     * 性别（0-保密，1-男，2-女）
     */
    @Schema(description = "性别（0-保密，1-男，2-女）")
    private Integer gender;

    /**
     * 所属院系/部门
     */
    @Schema(description = "所属院系")
    private String department;
}
