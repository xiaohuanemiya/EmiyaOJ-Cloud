package com.emiyaoj.auth.filter;

import com.emiyaoj.common.constant.JwtClaimsConstant;
import com.emiyaoj.common.properties.JwtProperties;
import com.emiyaoj.common.utils.BaseContext;
import com.emiyaoj.common.utils.JwtUtil;
import com.emiyaoj.common.utils.RedisUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Token 校验过滤器（微服务适配版）
 * <p>
 * 在 Auth 服务内部，对需要认证的请求进行 Token 校验。
 * 与原单体版本的主要区别：
 * 1. JWT 载荷使用结构化字段（userId / username / permissions），而非整个对象的 JSON 字符串
 * 2. 白名单路径使用 AntPathMatcher 进行匹配
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JwtTokenOncePerRequestFilter extends OncePerRequestFilter {

    private final JwtProperties jwtProperties;
    private final RedisUtil redisUtil;

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    /** 白名单路径 */
    private static final String[] WHITELIST = {
            "/auth/login",
            "/auth/user/parse-token",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/webjars/**",
            "/doc.html"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // OPTIONS 预检请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 白名单路径放行
        String uri = request.getRequestURI();
        if (isWhitelisted(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 优先从网关传递的 X-User-Id 头获取（说明网关已验证过 Token）
        String headerUserId = request.getHeader("X-User-Id");
        if (StringUtils.hasText(headerUserId)) {
            try {
                Long userId = Long.parseLong(headerUserId);
                BaseContext.setCurrentId(userId);
                // 构建认证对象
                String headerRoles = request.getHeader("X-User-Roles");
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (StringUtils.hasText(headerRoles)) {
                    for (String role : headerRoles.split(",")) {
                        authorities.add(new SimpleGrantedAuthority(role.trim()));
                    }
                }
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                filterChain.doFilter(request, response);
                return;
            } catch (NumberFormatException ignored) {
            }
        }

        // 直接访问 Auth 服务时，从 Authorization 头提取 Token 校验
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            sendUnauthorized(response, "Token 为空");
            return;
        }

        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            Long userId = claims.get(JwtClaimsConstant.USER_ID, Long.class);
            String username = claims.get(JwtClaimsConstant.USERNAME, String.class);

            // Redis 白名单校验
            String tokenKey = "token_" + userId;
            if (!redisUtil.hasKey(tokenKey)) {
                sendUnauthorized(response, "Token 已过期");
                return;
            }

            // 刷新 Token 有效期
            redisUtil.expire(tokenKey, jwtProperties.getTtl());

            BaseContext.setCurrentId(userId);

            // 构建权限列表
            @SuppressWarnings("unchecked")
            List<String> permissions = claims.get(JwtClaimsConstant.PERMISSIONS, List.class);
            List<SimpleGrantedAuthority> authorities = permissions == null ? List.of()
                    : permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            log.warn("Token 校验失败: {}", e.getMessage());
            sendUnauthorized(response, "Token 校验失败");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isWhitelisted(String uri) {
        for (String pattern : WHITELIST) {
            if (PATH_MATCHER.match(pattern, uri)) {
                return true;
            }
        }
        return false;
    }

    private String extractToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    }
}
