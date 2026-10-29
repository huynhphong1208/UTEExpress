package com.example.backend.config;

import com.example.backend.security.JwtAuthFilter;
import com.example.backend.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Cấu hình Spring Security - Stateless JWT, phân quyền theo Role
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService customUserDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Tắt CSRF vì dùng JWT stateless
            .csrf(AbstractHttpConfigurer::disable)

            // Stateless session
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Phân quyền Endpoints
            .authorizeHttpRequests(auth -> auth
                // Static Assets (CSS, JS, Images, Favicon, Error, Uploads)
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/uploads/**", "/favicon.ico", "/error").permitAll()
                
                // Public Web Views
                .requestMatchers("/", "/login", "/register", "/tracking", "/logout", "/design-system").permitAll()

                // WebSocket endpoint cho real-time tracking (public, guest cũng truy cập được)
                .requestMatchers("/ws-uteexpress/**").permitAll()

                // Public REST APIs
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tra-cuu/**").permitAll()

                // Phân quyền chi tiết cho cả Giao diện (Views) và REST APIs theo từng Vai trò (Roles)
                .requestMatchers("/admin/**", "/api/admin/**", "/api/v1/admin/**").hasAuthority("ADMIN")
                .requestMatchers("/post-office/**", "/api/v1/post-office/**", "/api/v1/payments/**").hasAnyAuthority("NVBC", "ADMIN")
                .requestMatchers("/dispatcher/**", "/api/dieu-phoi/**").hasAnyAuthority("NVDP", "ADMIN")
                .requestMatchers("/driver/**", "/api/tai-xe/**").hasAnyAuthority("TAI_XE", "ADMIN")
                .requestMatchers("/customer/**", "/api/khach-hang/**", "/api/don-hang/**").hasAnyAuthority("KHACH_HANG", "ADMIN")

                // Dispatcher Types cho Thymeleaf Views internal forwarding
                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.FORWARD, jakarta.servlet.DispatcherType.ERROR).permitAll()

                // Swagger UI
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs"
                ).permitAll()

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )

            // Xử lý khi chưa xác thực hoặc không có quyền: Chuyển hướng /login nếu duyệt web, trả JSON nếu gọi API
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    String uri = request.getRequestURI();
                    if (uri.startsWith("/api/")) {
                        response.setContentType("application/json;charset=UTF-8");
                        response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                        response.getWriter().write("{\"success\":false,\"message\":\"Chưa đăng nhập hoặc Token JWT không hợp lệ/hết hạn.\",\"status\":401}");
                    } else {
                        response.sendRedirect("/login");
                    }
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    String uri = request.getRequestURI();
                    if (uri.startsWith("/api/")) {
                        response.setContentType("application/json;charset=UTF-8");
                        response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
                        response.getWriter().write("{\"success\":false,\"message\":\"Tài khoản hiện tại không có quyền truy cập chức năng này (sai vai trò/role).\",\"status\":403}");
                    } else {
                        response.sendRedirect("/login?error=access_denied");
                    }
                })
            )

            // Đăng ký JWT Filter trước UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
