package com.example.campusactivity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类
 * 解决前端Vue项目与后端API的跨域问题
 */
@Configuration
public class CorsConfig {

    /**
     * 配置跨域过滤器
     * 允许前端8081端口访问后端API
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // 允许的前端域名
        config.addAllowedOriginPattern("*");
        // 允许携带Cookie
        config.setAllowCredentials(true);
        // 允许的请求方法
        config.addAllowedMethod("*");
        // 允许的请求头
        config.addAllowedHeader("*");
        // 暴露的响应头
        config.addExposedHeader("Authorization");
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
