package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * UTEExpress Logistics Backend
 * Khởi động ứng dụng Spring Boot
 */
@SpringBootApplication
@EnableAsync  // Bật gửi email bất đồng bộ
public class UteexpressApplication {

    public static void main(String[] args) {
        SpringApplication.run(UteexpressApplication.class, args);
    }
}
