package com.example.campusactivity.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.campusactivity.dto.LoginDTO;
import com.example.campusactivity.dto.PasswordDTO;
import com.example.campusactivity.dto.RegisterDTO;
import com.example.campusactivity.entity.User;
import com.example.campusactivity.vo.Result;

import java.util.Map;

/**
 * 用户服务接口
 * 提供用户相关的业务逻辑操作
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     * @param loginDTO 登录参数
     * @return Token和用户信息
     */
    Result<Map<String, Object>> login(LoginDTO loginDTO);

    /**
     * 用户注册（默认注册为学生角色）
     * @param registerDTO 注册参数
     * @return 注册结果
     */
    Result<Void> register(RegisterDTO registerDTO);

    /**
     * 获取当前登录用户信息
     * @return 用户信息（包含角色）
     */
    Result<User> getCurrentUser();

    /**
     * 修改密码
     * @param passwordDTO 密码参数
     * @return 操作结果
     */
    Result<Void> updatePassword(PasswordDTO passwordDTO);

    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 操作结果
     */
    Result<Void> updateUserInfo(User user);

    /**
     * 分页查询用户列表
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @param keyword 搜索关键词
     * @param roleCode 角色编码筛选
     * @return 分页结果
     */
    Result<IPage<User>> listUsers(Integer pageNum, Integer pageSize, String keyword, String roleCode);

    /**
     * 禁用/启用用户
     * @param userId 用户ID
     * @param status 目标状态
     * @return 操作结果
     */
    Result<Void> updateUserStatus(Long userId, Integer status);
}
