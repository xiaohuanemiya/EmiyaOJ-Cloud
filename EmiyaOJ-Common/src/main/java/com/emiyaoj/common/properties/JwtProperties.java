package com.emiyaoj.common.properties;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 相关属性配置 — 仅在配置了 jwt.secret-key 时生效
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
@ConditionalOnProperty(prefix = "jwt", name = "secret-key")
public class JwtProperties {

    /** 签名密钥 */
    private String secretKey;

    /** 过期时间（毫秒） */
    private long ttl;

    /** 请求头中 Token 名称 */
    private String tokenName = "Authorization";
}
