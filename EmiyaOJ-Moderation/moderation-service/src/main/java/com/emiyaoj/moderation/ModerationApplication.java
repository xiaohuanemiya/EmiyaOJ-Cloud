package com.emiyaoj.moderation;

import com.emiyaoj.moderation.config.ModerationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = {"com.emiyaoj.moderation", "com.emiyaoj.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.emiyaoj.blog.api")
@EnableConfigurationProperties(ModerationProperties.class)
public class ModerationApplication {

    public static void main(String[] args) {
        SpringApplication.run(ModerationApplication.class, args);
    }
}
