package com.emiyaoj.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "emiyaoj.moderation")
public class BlogModerationProperties {

    public static final String INTERNAL_TOKEN_HEADER = "X-Moderation-Token";
    public static final String MANAGE_PERMISSION = "BLOG_MODERATION_MANAGE";

    private String internalToken = "emiyaoj-moderation-internal";
}
