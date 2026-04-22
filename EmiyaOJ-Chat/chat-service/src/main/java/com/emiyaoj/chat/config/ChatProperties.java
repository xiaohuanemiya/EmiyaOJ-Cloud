package com.emiyaoj.chat.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI Chat 配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "chat")
public class ChatProperties {

    /** DashScope API Key */
    private String apiKey;

    /** DashScope API URL */
    private String apiUrl;

    /** 模型名称 */
    private String model;
}
