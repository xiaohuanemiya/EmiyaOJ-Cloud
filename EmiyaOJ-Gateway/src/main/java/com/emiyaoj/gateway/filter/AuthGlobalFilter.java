package com.emiyaoj.gateway.filter;

import com.emiyaoj.gateway.config.GatewayWhitelistProperties;
import com.emiyaoj.gateway.config.JwtProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 网关全局认证过滤器
 * <p>
 * 核心逻辑：
 * 1. 白名单路径直接放行
 * 2. 从 Authorization 请求头提取 Bearer Token
 * 3. 在网关层直接解析 JWT（无需 Feign 调用 Auth 服务），获取 userId、username、permissions
 * 4. 验证 Redis Token 白名单
 * 5. 将 X-User-Id、X-User-Name、X-User-Roles 写入请求头转发给下游服务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtProperties jwtProperties;
    private final GatewayWhitelistProperties whitelistProperties;
    private final ReactiveStringRedisTemplate reactiveRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1. 白名单路径直接放行
        if (isWhitelisted(path)) {
            return chain.filter(exchange);
        }

        // 2. 提取 Token
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            return unauthorizedResponse(exchange, "缺少认证Token");
        }

        // 3. 解析 JWT
        Claims claims;
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
            claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.warn("网关 Token 解析失败: {}", e.getMessage());
            return unauthorizedResponse(exchange, "Token无效或已过期");
        }

        // 4. 从 Claims 中提取用户信息
        Object userIdObj = claims.get("userId");
        Object usernameObj = claims.get("username");
        Object permissionsObj = claims.get("permissions");

        if (userIdObj == null) {
            return unauthorizedResponse(exchange, "Token载荷异常");
        }

        String userId = String.valueOf(userIdObj);
        String username = usernameObj != null ? String.valueOf(usernameObj) : "";
        String permissions = "";
        if (permissionsObj instanceof List<?> permList) {
            permissions = String.join(",", permList.stream().map(String::valueOf).toList());
        }

        // 5. 验证 Redis Token 白名单并刷新有效期
        String tokenKey = "token_" + userId;
        String finalPermissions = permissions;

        return reactiveRedisTemplate.hasKey(tokenKey)
                .flatMap(exists -> {
                    if (Boolean.FALSE.equals(exists)) {
                        return unauthorizedResponse(exchange, "Token已过期或已注销");
                    }
                    // 刷新 Token 有效期
                    return reactiveRedisTemplate.expire(tokenKey, Duration.ofMillis(jwtProperties.getTtl()))
                            .then(Mono.defer(() -> {
                                // 6. 将用户信息写入请求头转发给下游
                                ServerHttpRequest mutatedRequest = request.mutate()
                                        .header("X-User-Id", userId)
                                        .header("X-User-Name", username)
                                        .header("X-User-Roles", finalPermissions)
                                        .build();
                                return chain.filter(exchange.mutate().request(mutatedRequest).build());
                            }));
                });
    }

    @Override
    public int getOrder() {
        return -100; // 优先级最高
    }

    /**
     * 判断请求路径是否在白名单中
     */
    private boolean isWhitelisted(String path) {
        List<String> whitelist = whitelistProperties.getWhitelist();
        if (whitelist == null || whitelist.isEmpty()) {
            return false;
        }
        return whitelist.stream().anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    /**
     * 从请求头中提取 Token（去掉 Bearer 前缀）
     */
    private String extractToken(ServerHttpRequest request) {
        String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }

    /**
     * 返回 401 响应
     */
    private Mono<Void> unauthorizedResponse(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(Map.of("code", 401, "message", message, "data", ""));
        } catch (JsonProcessingException e) {
            bytes = ("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}").getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}
