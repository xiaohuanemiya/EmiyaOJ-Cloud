package com.emiyaoj.common.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 配置 — Token 透传与用户上下文透传
 * <p>
 * 在微服务间通过 Feign 调用时，自动将当前请求的 Authorization 头和网关注入的 X-User-* 头透传到下游服务。
 */
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate template) -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                // 透传 Authorization
                String authorization = request.getHeader("Authorization");
                if (authorization != null) {
                    template.header("Authorization", authorization);
                }
                // 透传网关注入的用户信息头
                String userId = request.getHeader("X-User-Id");
                if (userId != null) {
                    template.header("X-User-Id", userId);
                }
                String userName = request.getHeader("X-User-Name");
                if (userName != null) {
                    template.header("X-User-Name", userName);
                }
                String userRoles = request.getHeader("X-User-Roles");
                if (userRoles != null) {
                    template.header("X-User-Roles", userRoles);
                }
            }
        };
    }
}
