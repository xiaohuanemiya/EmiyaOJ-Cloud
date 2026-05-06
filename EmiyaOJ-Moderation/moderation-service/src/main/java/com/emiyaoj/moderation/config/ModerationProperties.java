package com.emiyaoj.moderation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
@ConfigurationProperties(prefix = "emiyaoj.moderation")
public class ModerationProperties {

    private String endpoint = "imageaudit.cn-shanghai.aliyuncs.com";

    private String internalToken = "emiyaoj-moderation-internal";

    private int maxCallbackRetries = 3;

    private long callbackRetryIntervalMillis = 1000L;

    private List<String> labels = List.of(
            "spam",
            "politics",
            "abuse",
            "terrorism",
            "porn",
            "flood",
            "contraband",
            "ad"
    );
}
