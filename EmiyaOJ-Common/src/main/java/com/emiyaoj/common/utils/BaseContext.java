package com.emiyaoj.common.utils;

/**
 * ThreadLocal 上下文工具 — 存储当前登录用户 ID
 */
public class BaseContext {

    private static final ThreadLocal<Long> CURRENT_ID = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        CURRENT_ID.set(id);
    }

    public static Long getCurrentId() {
        return CURRENT_ID.get();
    }

    public static void remove() {
        CURRENT_ID.remove();
    }
}
