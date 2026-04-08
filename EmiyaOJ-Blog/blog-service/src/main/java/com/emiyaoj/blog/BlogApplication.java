package com.emiyaoj.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 博客服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.emiyaoj.blog", "com.emiyaoj.common"})
@EnableDiscoveryClient
@MapperScan("com.emiyaoj.blog.mapper")
public class BlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
