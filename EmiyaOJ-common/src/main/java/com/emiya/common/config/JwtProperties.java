package com.emiya.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secretKey;
    private long expiration = 3600; // 默认1小时
}

