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
                // Public: Auth APIs
                .requestMatchers("/api/auth/**").permitAll()

                // Public: Tra cứu công khai
                .requestMatchers(HttpMethod.GET, "/api/tra-cuu/**").permitAll()

                // Swagger UI
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs"
                ).permitAll()

                // Admin APIs
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")

                // Customer APIs
                .requestMatchers("/api/khach-hang/**").hasAuthority("KHACH_HANG")

                // Order creation - KHACH_HANG
                .requestMatchers(HttpMethod.POST, "/api/don-hang").hasAuthority("KHACH_HANG")

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )

            // Đăng ký JWT Filter trước UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
