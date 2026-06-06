package com.example.campusactivity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.campusactivity.dto.LoginDTO;
import com.example.campusactivity.dto.PasswordDTO;
import com.example.campusactivity.dto.RegisterDTO;
import com.example.campusactivity.entity.User;
import com.example.campusactivity.service.UserService;
import com.example.campusactivity.vo.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 * 处理用户相关的HTTP请求：登录、注册、个人信息管理
 * RESTful API设计，路径语义清晰
 */
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户登录、注册、个人信息管理接口")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     * POST /api/user/login
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回JWT Token")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    /**
     * 用户注册
     * POST /api/user/register
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新账号，默认角色为学生")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return userService.register(registerDTO);
    }

    /**
     * 获取当前登录用户信息
     * GET /api/user/info
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息", description = "根据Token获取当前登录用户的详细信息")
    public Result<User> getCurrentUser() {
        return userService.getCurrentUser();
    }

    /**
     * 修改密码
     * PUT /api/user/password
     */
    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordDTO passwordDTO) {
        return userService.updatePassword(passwordDTO);
    }

    /**
     * 更新用户信息
     * PUT /api/user/info
     */
    @PutMapping("/info")
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的个人信息")
    public Result<Void> updateUserInfo(@RequestBody User user) {
        return userService.updateUserInfo(user);
    }

    /**
     * 分页查询用户列表（管理员权限）
     * GET /api/user/list
     */
    @GetMapping("/list")
    @Operation(summary = "查询用户列表", description = "管理员查询所有用户，支持分页和搜索")
    public Result<IPage<User>> listUsers(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "角色编码筛选") @RequestParam(required = false) String roleCode) {
        return userService.listUsers(pageNum, pageSize, keyword, roleCode);
    }

    /**
     * 禁用/启用用户（管理员权限）
     * PUT /api/user/status/{userId}
     */
    @PutMapping("/status/{userId}")
    @Operation(summary = "修改用户状态", description = "管理员禁用或启用用户账号")
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long userId,
            @Parameter(description = "状态（0-禁用，1-启用）") @RequestParam Integer status) {
        return userService.updateUserStatus(userId, status);
    }
}
