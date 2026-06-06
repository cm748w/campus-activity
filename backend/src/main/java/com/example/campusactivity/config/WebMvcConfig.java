package com.example.campusactivity.config;

import com.example.campusactivity.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC配置类
 * 注册JWT认证拦截器，配置拦截路径规则
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    /**
     * 配置拦截器
     * 排除登录、注册、Swagger文档等公开接口
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                // 拦截所有请求
                .addPathPatterns("/**")
                // 排除登录相关接口
                .excludePathPatterns(
                        "/user/login",
                        "/user/register",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/doc.html/**",
                        "/webjars/**",
                        "/favicon.ico"
                );
    }
}
