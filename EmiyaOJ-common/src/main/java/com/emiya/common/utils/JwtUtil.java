package com.emiya.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Map;

/**
 * JWT工具类
 * 用于生成和解析JWT token
 */
@Slf4j
public class JwtUtil {

    /**
     * 生成JWT token
     * @param secretKey 密钥
     * @param claims 声明数据
     * @param expirationSeconds 过期时间（秒）
     * @return token字符串
     */
    public static String createJWT(String secretKey, Map<String, Object> claims, long expirationSeconds) {
        long expirationMillis = System.currentTimeMillis() + expirationSeconds * 1000;
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(expirationMillis))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    /**
     * 生成JWT token（无自定义过期时间，使用默认值）
     * @param secretKey 密钥
     * @param claims 声明数据
     * @return token字符串
     */
    public static String createJWT(String secretKey, Map<String, Object> claims) {
        return createJWT(secretKey, claims, 3600); // 默认1小时
    }

    /**
     * 解析JWT token
     * @param secretKey 密钥
     * @param token token字符串
     * @return Claims 声明数据
     */
    public static Claims parseJWT(String secretKey, String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.error("Failed to parse JWT token: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 验证JWT token是否有效
     * @param secretKey 密钥
     * @param token token字符串
     * @return 是否有效
     */
    public static boolean validateToken(String secretKey, String token) {
        try {
            Jwts.parser()
                    .setSigningKey(secretKey)
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取JWT token中的过期时间
     * @param secretKey 密钥
     * @param token token字符串
     * @return 过期时间戳（毫秒）
     */
    public static long getExpirationTime(String secretKey, String token) {
        Claims claims = parseJWT(secretKey, token);
        return claims.getExpiration().getTime();
    }
}

