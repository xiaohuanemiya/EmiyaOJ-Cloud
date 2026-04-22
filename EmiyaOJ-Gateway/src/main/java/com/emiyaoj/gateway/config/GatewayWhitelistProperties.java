package com.emiyaoj.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 网关白名单配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "gateway")
public class GatewayWhitelistProperties {

    /** 无需认证的路径列表 */
    private List<String> whitelist;
}
