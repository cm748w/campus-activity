package com.example.campusactivity.interceptor;

import com.example.campusactivity.entity.User;
import com.example.campusactivity.exception.BusinessException;
import com.example.campusactivity.service.UserService;
import com.example.campusactivity.utils.JwtUtil;
import com.example.campusactivity.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT认证拦截器
 * 拦截所有请求，验证请求头中的JWT Token
 * 将解析后的用户信息存入ThreadLocal供后续使用
 */
@Slf4j
@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    /**
     * 请求处理前执行Token验证
     * 从请求头中获取Authorization，解析并验证Token
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从请求头获取Token
        String token = request.getHeader("Authorization");
        
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("请求缺少Authorization头或格式错误: {}", request.getRequestURI());
            throw new BusinessException(401, "请先登录");
        }

        // 提取Token（去掉Bearer前缀）
        token = token.substring(7);

        // 验证Token有效性
        if (!jwtUtil.validateToken(token)) {
            log.warn("Token验证失败: {}", request.getRequestURI());
            throw new BusinessException(401, "登录已过期，请重新登录");
        }

        // 从Token解析用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        
        // 查询用户信息
        User user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }

        // 检查账号状态
        if (user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用，请联系管理员");
        }

        // 将用户信息存入ThreadLocal
        UserContext.setUser(user);
        
        return true;
    }

    /**
     * 请求完成后清理ThreadLocal，防止内存泄漏
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
