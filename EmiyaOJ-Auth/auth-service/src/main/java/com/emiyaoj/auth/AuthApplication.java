package com.emiyaoj.auth;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.emiyaoj.auth", "com.emiyaoj.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.emiyaoj.judge.api", "com.emiyaoj.blog.api"})
@MapperScan("com.emiyaoj.auth.mapper")
public class AuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthApplication.class, args);
    }
}
