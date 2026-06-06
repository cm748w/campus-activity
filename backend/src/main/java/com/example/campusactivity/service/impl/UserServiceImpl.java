package com.example.campusactivity.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.campusactivity.dto.LoginDTO;
import com.example.campusactivity.dto.PasswordDTO;
import com.example.campusactivity.dto.RegisterDTO;
import com.example.campusactivity.entity.Role;
import com.example.campusactivity.entity.User;
import com.example.campusactivity.entity.UserRole;
import com.example.campusactivity.exception.BusinessException;
import com.example.campusactivity.mapper.RoleMapper;
import com.example.campusactivity.mapper.UserMapper;
import com.example.campusactivity.mapper.UserRoleMapper;
import com.example.campusactivity.service.UserService;
import com.example.campusactivity.utils.JwtUtil;
import com.example.campusactivity.utils.UserContext;
import com.example.campusactivity.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户服务实现类
 * 实现用户相关的业务逻辑
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RoleMapper roleMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户登录
     * 验证用户名密码，生成JWT Token
     */
    @Override
    public Result<Map<String, Object>> login(LoginDTO loginDTO) {
        // 查询用户
        User user = userMapper.selectByUsernameWithRole(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(400, "用户名或密码错误");
        }

        // 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }

        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(400, "用户名或密码错误");
        }

        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRoleCode());

        // 构建返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("tokenType", "Bearer");

        // 用户信息脱敏返回
        user.setPassword(null);
        result.put("userInfo", user);

        log.info("用户登录成功: {}", user.getUsername());
        return Result.success("登录成功", result);
    }

    /**
     * 用户注册
     * 默认注册为学生角色
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> register(RegisterDTO registerDTO) {
        // 校验两次密码是否一致
        if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
            return Result.error("两次输入的密码不一致");
        }

        // 检查用户名是否已存在
        User existUser = lambdaQuery().eq(User::getUsername, registerDTO.getUsername()).one();
        if (existUser != null) {
            return Result.error("该用户名已被注册");
        }

        // 检查手机号是否已存在
        if (StringUtils.hasText(registerDTO.getPhone())) {
            User phoneUser = lambdaQuery().eq(User::getPhone, registerDTO.getPhone()).one();
            if (phoneUser != null) {
                return Result.error("该手机号已被注册");
            }
        }

        // 创建用户
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setStatus(1); // 默认启用
        userMapper.insert(user);

        // 分配学生角色（role_id = 3）
        UserRole userRole = new UserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(3L); // 学生角色
        userRoleMapper.insert(userRole);

        log.info("用户注册成功: {}", user.getUsername());
        return Result.success("注册成功，请登录");
    }

    /**
     * 获取当前登录用户信息
     */
    @Override
    public Result<User> getCurrentUser() {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }

        // 重新查询获取完整信息（包括角色）
        User user = userMapper.selectByUsernameWithRole(currentUser.getUsername());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        user.setPassword(null); // 脱敏
        return Result.success(user);
    }

    /**
     * 修改密码
     */
    @Override
    public Result<Void> updatePassword(PasswordDTO passwordDTO) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }

        // 校验新密码
        if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
            return Result.error("两次输入的新密码不一致");
        }

        // 查询用户
        User user = getById(currentUser.getId());
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            return Result.error("旧密码不正确");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        updateById(user);

        return Result.success("密码修改成功，请重新登录");
    }

    /**
     * 更新用户信息
     */
    @Override
    public Result<Void> updateUserInfo(User user) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }

        // 只能修改自己的信息
        user.setId(currentUser.getId());
        // 不允许修改的字段
        user.setUsername(null);
        user.setPassword(null);
        user.setStatus(null);
        user.setCreateTime(null);

        updateById(user);
        return Result.success("个人信息更新成功");
    }

    /**
     * 分页查询用户列表
     */
    @Override
    public Result<IPage<User>> listUsers(Integer pageNum, Integer pageSize, String keyword, String roleCode) {
        Page<User> page = new Page<>(pageNum, pageSize);
        IPage<User> userPage = userMapper.selectUserPage(page, keyword, roleCode);

        // 清除密码信息
        userPage.getRecords().forEach(user -> user.setPassword(null));

        return Result.success(userPage);
    }

    /**
     * 禁用/启用用户
     */
    @Override
    public Result<Void> updateUserStatus(Long userId, Integer status) {
        User currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(401, "请先登录");
        }

        // 不能操作自己
        if (currentUser.getId().equals(userId)) {
            return Result.error("不能操作自己的账号");
        }

        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        user.setStatus(status);
        updateById(user);

        String action = status == 1 ? "启用" : "禁用";
        log.info("管理员{}用户: {}", action, user.getUsername());
        return Result.success(action + "成功");
    }
}
