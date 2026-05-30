package com.emiyaoj.problem;

import com.emiyaoj.problem.config.AppFileProperties;
import com.emiyaoj.problem.config.MinioProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 题目服务启动类
 */
@SpringBootApplication(scanBasePackages = {"com.emiyaoj.problem", "com.emiyaoj.common"})
@EnableDiscoveryClient
@MapperScan("com.emiyaoj.problem.mapper")
@EnableFeignClients(basePackages = {"com.emiyaoj.auth.api", "com.emiyaoj.judge.api"})
@EnableConfigurationProperties({MinioProperties.class, AppFileProperties.class})
public class ProblemApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProblemApplication.class, args);
    }
}
