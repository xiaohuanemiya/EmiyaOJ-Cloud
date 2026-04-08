package com.emiyaoj.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类 — 封装常用操作
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 设置 key-value，带过期时间（毫秒）
     */
    public void set(String key, String value, long ttlMillis) {
        stringRedisTemplate.opsForValue().set(key, value, ttlMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取 value
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 判断 key 是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    /**
     * 删除 key
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }

    /**
     * 设置过期时间（毫秒）
     */
    public boolean expire(String key, long ttlMillis) {
        return Boolean.TRUE.equals(stringRedisTemplate.expire(key, ttlMillis, TimeUnit.MILLISECONDS));
    }

    /**
     * 自增
     */
    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }
}
