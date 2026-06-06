package com.example.campusactivity.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * 提供Token的生成、解析、验证等功能
 * 用于用户认证和授权
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * 生成安全的SecretKey
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成JWT Token
     * @param userId 用户ID
     * @param username 用户名
     * @param roleCode 角色编码
     * @return JWT字符串
     */
    public String generateToken(Long userId, String username, String roleCode) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("roleCode", roleCode);
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 从Token中解析用户ID
     * @param token JWT字符串
     * @return 用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.valueOf(claims.get("userId").toString());
    }

    /**
     * 从Token中解析用户名
     * @param token JWT字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username").toString();
    }

    /**
     * 从Token中解析角色编码
     * @param token JWT字符串
     * @return 角色编码
     */
    public String getRoleCodeFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("roleCode").toString();
    }

    /**
     * 解析Token获取Claims
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 验证Token是否有效
     * @param token JWT字符串
     * @return true-有效，false-无效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT Token已过期: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("不支持的JWT Token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("JWT Token格式错误: {}", e.getMessage());
        } catch (SignatureException e) {
            log.warn("JWT签名验证失败: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT Token为空或非法: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 判断Token是否即将过期（剩余时间小于1小时）
     * @param token JWT字符串
     * @return true-即将过期
     */
    public boolean isTokenExpiredSoon(String token) {
        try {
            Claims claims = parseToken(token);
            Date expiration = claims.getExpiration();
            // 剩余时间小于1小时则视为即将过期
            return (expiration.getTime() - System.currentTimeMillis()) < 3600000;
        } catch (Exception e) {
            return true;
        }
    }
}
