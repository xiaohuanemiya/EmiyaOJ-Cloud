package com.emiya.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * JWT 自动配置
 * 只在配置了 jwt.secret-key 时才启用
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
@ConditionalOnProperty(prefix = "jwt", name = "secret-key")
public class JwtAutoConfiguration {
}

