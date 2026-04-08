package com.emiyaoj.common.interceptor;

import com.emiyaoj.common.utils.BaseContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户上下文拦截器 — 从网关转发的请求头中提取用户信息，写入 ThreadLocal
 * <p>
 * 网关层已完成 Token 解析，并将 X-User-Id、X-User-Name、X-User-Roles 放入请求头。
 * 下游微服务通过此拦截器自动填充上下文。
 */
public class UserContextInterceptor implements HandlerInterceptor {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_NAME = "X-User-Name";
    public static final String HEADER_USER_ROLES = "X-User-Roles";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userId = request.getHeader(HEADER_USER_ID);
        if (StringUtils.hasText(userId)) {
            try {
                BaseContext.setCurrentId(Long.parseLong(userId));
            } catch (NumberFormatException ignored) {
                // 忽略非法 userId
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.remove();
    }
}
