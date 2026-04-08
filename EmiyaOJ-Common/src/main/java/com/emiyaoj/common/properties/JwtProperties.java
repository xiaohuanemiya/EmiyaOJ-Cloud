package com.emiyaoj.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 相关属性配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /** 签名密钥 */
    private String secretKey;

    /** 过期时间（毫秒） */
    private long ttl;

    /** 请求头中 Token 名称 */
    private String tokenName = "Authorization";
}
