package com.emiyaoj.moderation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AliyunImageAuditConfig {

    @Bean
    public com.aliyun.imageaudit20191230.Client imageAuditClient(ModerationProperties properties) throws Exception {
        com.aliyun.credentials.Client credential = new com.aliyun.credentials.Client();
        com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                .setCredential(credential);
        config.endpoint = properties.getEndpoint();
        return new com.aliyun.imageaudit20191230.Client(config);
    }
}
