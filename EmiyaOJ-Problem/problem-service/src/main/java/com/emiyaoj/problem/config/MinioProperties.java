package com.emiyaoj.problem.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "emiyaoj.minio")
public class MinioProperties {

    private String endpoint = "http://127.0.0.1:9000";

    private String publicEndpoint = "http://127.0.0.1:9000";

    private String accessKey = "minioadmin";

    private String secretKey = "minioadmin";

    private String bucket = "problem-images";

    private Long maxFileSize = 10 * 1024 * 1024L;
}
