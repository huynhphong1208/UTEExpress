package com.example.backend.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Swagger / OpenAPI với hỗ trợ Bearer Token
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("UTEExpress Logistics API")
                        .description("Backend API cho hệ thống Logistics UTEExpress - Bao gồm xác thực, " +
                                "quản lý đơn hàng, tra cứu công khai và quản trị người dùng.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("UTEExpress Team")
                                .email("admin@uteexpress.vn")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập JWT token: Bearer <token>")));
    }
}
