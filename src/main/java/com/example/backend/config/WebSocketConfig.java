package com.example.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Cấu hình WebSocket Message Broker cho tính năng Real-time Tracking.
 *
 * - Sử dụng giao thức STOMP (Simple Text Oriented Messaging Protocol) trên nền SockJS
 *   để đảm bảo tương thích trình duyệt (fallback khi WebSocket không khả dụng).
 * - Client kết nối qua endpoint "/ws-uteexpress", subscribe các channel "/topic/..."
 *   để nhận cập nhật trạng thái đơn hàng thời gian thực.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Đăng ký STOMP endpoint để client kết nối.
     * - "/ws-uteexpress": endpoint chính, hỗ trợ SockJS fallback
     * - setAllowedOriginPatterns("*"): cho phép mọi origin kết nối (dev/staging)
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-uteexpress")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    /**
     * Cấu hình Message Broker nội bộ (in-memory).
     * - "/topic": channel broadcast (1-to-many), dùng cho tracking đơn hàng
     * - "/queue": channel point-to-point (1-to-1), dùng cho thông báo cá nhân (mở rộng)
     * - "/app": tiền tố cho message từ client gửi lên server (@MessageMapping)
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }
}
