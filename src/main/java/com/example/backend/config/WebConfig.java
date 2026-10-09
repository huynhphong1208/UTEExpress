package com.example.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Hỗ trợ cả đường dẫn tương đối ../../static/ và /static/
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");

        // Cho phép truy cập trực tiếp các file template tĩnh dạng HTML nếu mở từ trình duyệt
        registry.addResourceHandler("/dispatcher/**")
                .addResourceLocations("classpath:/templates/dispatcher/");
        registry.addResourceHandler("/driver/**")
                .addResourceLocations("classpath:/templates/driver/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addRedirectViewController("/", "/dispatcher/12-dashboard.html");
        registry.addViewController("/dispatcher/dashboard").setViewName("forward:/dispatcher/12-dashboard.html");
        registry.addViewController("/dispatcher/routes").setViewName("forward:/dispatcher/13-routes.html");
        registry.addViewController("/dispatcher/trips").setViewName("forward:/dispatcher/14-trips.html");
        registry.addViewController("/driver/dashboard").setViewName("forward:/driver/15-dashboard.html");
        registry.addViewController("/driver/trip-detail").setViewName("forward:/driver/16-trip-detail.html");
        registry.addViewController("/driver/delivery-update").setViewName("forward:/driver/17-delivery-update.html");
    }
}
