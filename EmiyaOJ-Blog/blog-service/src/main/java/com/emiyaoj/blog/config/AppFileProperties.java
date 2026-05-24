package com.emiyaoj.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.file")
public class AppFileProperties {

    private String publicBaseUrl = "http://127.0.0.1:9000";

    private String bucket = "blog-images";
}
