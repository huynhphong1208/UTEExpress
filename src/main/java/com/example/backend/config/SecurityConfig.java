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
                // Static Assets (CSS, JS, Images, Favicon)
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/error").permitAll()
                
                // HTML Web View Pages (Rendered by Thymeleaf)
                .requestMatchers("/", "/login", "/register", "/tracking", "/customer/**", "/admin/**").permitAll()

                // Public REST APIs
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/tra-cuu/**").permitAll()

                // Dispatcher Types cho Thymeleaf Views
                .dispatcherTypeMatchers(jakarta.servlet.DispatcherType.FORWARD, jakarta.servlet.DispatcherType.ERROR).permitAll()

                // Static resources & UI Views
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/error").permitAll()
                .requestMatchers("/customer/**", "/post-office/**", "/admin/**", "/", "/index").permitAll()

                // Swagger UI
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs"
                ).permitAll()

                // Protected REST APIs (Secured by JWT Token)
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                .requestMatchers("/api/khach-hang/**").hasAuthority("KHACH_HANG")
                .requestMatchers(HttpMethod.POST, "/api/don-hang").hasAuthority("KHACH_HANG")

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )

            // Xử lý phản hồi JSON rõ ràng khi chưa xác thực hoặc không có quyền (thay vì trả về trống)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("{\"success\":false,\"message\":\"Chưa đăng nhập hoặc Token JWT không hợp lệ/hết hạn. Vui lòng đăng nhập (với tài khoản có vai trò phù hợp như KHACH_HANG) và gắn Token vào nút Authorize!\",\"status\":401}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"success\":false,\"message\":\"Tài khoản hiện tại không có quyền truy cập chức năng này (sai vai trò/role).\",\"status\":403}");
                })
            )

            // Đăng ký JWT Filter trước UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
