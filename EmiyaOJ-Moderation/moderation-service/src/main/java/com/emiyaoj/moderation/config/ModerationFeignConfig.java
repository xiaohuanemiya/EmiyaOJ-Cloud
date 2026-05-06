package com.emiyaoj.moderation.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModerationFeignConfig {

    public static final String INTERNAL_TOKEN_HEADER = "X-Moderation-Token";

    @Bean
    public RequestInterceptor moderationTokenInterceptor(ModerationProperties properties) {
        return template -> template.header(INTERNAL_TOKEN_HEADER, properties.getInternalToken());
    }
}
