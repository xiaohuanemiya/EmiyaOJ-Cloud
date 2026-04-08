package com.emiya.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;

    public boolean set(String key, Object value) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");
        redisTemplate.opsForValue().set(key, value);
        return true;
    }

    public boolean set(String key, Object value, long timeout, TimeUnit timeUnit) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(timeUnit, "timeUnit must not be null");
        if (timeout <= 0) {
            return set(key, value);
        }
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
        return true;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        Objects.requireNonNull(key, "key must not be null");
        return (T) redisTemplate.opsForValue().get(key);
    }

    public Boolean delete(String key) {
        Objects.requireNonNull(key, "key must not be null");
        return redisTemplate.delete(key);
    }

    public Boolean hasKey(String key) {
        Objects.requireNonNull(key, "key must not be null");
        return redisTemplate.hasKey(key);
    }

    public Boolean expire(String key, long timeout, TimeUnit timeUnit) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(timeUnit, "timeUnit must not be null");
        if (timeout <= 0) {
            return false;
        }
        return redisTemplate.expire(key, timeout, timeUnit);
    }

    public Long getExpire(String key, TimeUnit timeUnit) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(timeUnit, "timeUnit must not be null");
        return redisTemplate.getExpire(key, timeUnit);
    }

    public Long increment(String key, long delta) {
        Objects.requireNonNull(key, "key must not be null");
        if (delta <= 0) {
            throw new IllegalArgumentException("delta must be greater than 0");
        }
        return redisTemplate.opsForValue().increment(key, delta);
    }

    public Long decrement(String key, long delta) {
        Objects.requireNonNull(key, "key must not be null");
        if (delta <= 0) {
            throw new IllegalArgumentException("delta must be greater than 0");
        }
        return redisTemplate.opsForValue().decrement(key, delta);
    }
}
