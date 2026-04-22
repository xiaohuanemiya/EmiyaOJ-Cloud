package com.emiyaoj.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 统一配置
 * <p>
 * 提供全局的 OpenAPI 文档信息（标题、版本、安全方案等），
 * 各微服务可通过 {@code springdoc.group-configs} 做分组定制。
 * <p>
 * 仅在 Servlet Web 应用且 classpath 包含 {@code OpenAPI} 类时生效。
 */
@Configuration
@ConditionalOnClass(OpenAPI.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Token";

    @Bean
    public OpenAPI emiyaOjOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EmiyaOJ API")
                        .description("EmiyaOJ 在线评测系统 — 微服务接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("EmiyaOJ Team")
                                .url("https://github.com/xiaohuanemiya/EmiyaOJ-Cloud"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                // 全局安全方案：Bearer Token
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("在此输入 JWT Token（不需要 Bearer 前缀）")));
    }
}
