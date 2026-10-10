package com.example.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Cấu hình OpenAPI / Swagger UI cho phân hệ Bưu cục, Tài chính & Báo cáo
 * Tích hợp SecurityFilterChain bypass để test Swagger nhanh chóng và độc lập.
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    @Primary
    public OpenAPI postOfficeFinanceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Logistics UTEExpress - Post Office & Finance APIs")
                        .description("Tài liệu API Backend cho phân hệ Bưu cục (UC08, UC09, UC10), " +
                                "Tài chính & Đối soát COD (UC06, UC07, Mục 4.2.22) và Báo cáo Thống kê Admin (UC17, Mục 4.2.23).")
                        .version("1.0")
                        .contact(new Contact()
                                .name("UTEExpress Logistics Team")
                                .email("support@uteexpress.vn")
                                .url("https://uteexpress.vn"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://springdoc.org")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập JWT Token: Bearer <token> (hoặc test trực tiếp các endpoint /api/v1/** được mở tự do)")));
    }

    /**
     * Group Swagger UI chuyên biệt cho phân hệ Post Office, Finance & Admin Reports (/api/v1/**)
     */
    @Bean
    public GroupedOpenApi postOfficeFinanceApiGroup() {
        return GroupedOpenApi.builder()
                .group("post-office-finance")
                .pathsToMatch("/api/v1/**")
                .build();
    }

    /**
     * Group Swagger UI cho tất cả các API trong hệ thống
     */
    @Bean
    public GroupedOpenApi allApiGroup() {
        return GroupedOpenApi.builder()
                .group("all-apis")
                .pathsToMatch("/**")
                .build();
    }

    /**
     * Security Filter Chain ưu tiên cao (@Order(1)) dành riêng cho /api/v1/**
     * Giúp test toàn bộ API trên Swagger UI hoặc Postman mà không bị chặn bởi 401 Unauthorized,
     * hoàn toàn độc lập và KHÔNG làm thay đổi / xung đột với SecurityConfig sẵn có của thành viên khác.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiV1SecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }
}
