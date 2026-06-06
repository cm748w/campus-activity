package com.example.campusactivity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户登录请求DTO
 * 接收前端登录表单数据
 */
@Data
@Schema(description = "用户登录请求")
public class LoginDTO {

    /**
     * 用户名/学号/工号
     */
    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名/学号/工号", required = true)
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", required = true)
    private String password;
}
