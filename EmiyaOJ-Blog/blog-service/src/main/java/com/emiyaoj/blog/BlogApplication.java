package com.emiyaoj.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import com.emiyaoj.blog.config.BlogModerationProperties;
import com.emiyaoj.blog.config.AppFileProperties;
import com.emiyaoj.blog.config.MinioProperties;

/**
 * 博客服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.emiyaoj.blog", "com.emiyaoj.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.emiyaoj.problem.api")
@EnableConfigurationProperties({MinioProperties.class, AppFileProperties.class, BlogModerationProperties.class})
@MapperScan("com.emiyaoj.blog.mapper")
public class BlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
