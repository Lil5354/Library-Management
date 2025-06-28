
package com.uef.library.config;

import com.uef.library.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    @Lazy
    private CustomLoginSuccessHandler loginSuccessHandler;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Cấu hình bảo mật cho các API endpoint.
     * Được ưu tiên xử lý trước (@Order(1)).
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/readers**") // Chỉ áp dụng cho các đường dẫn bắt đầu bằng /api/
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // === THAY ĐỔI QUAN TRỌNG ĐỂ SỬA LỖI 403 ===
                        // Cho phép truy cập công khai tất cả các API cần thiết cho việc reset password
                        .requestMatchers("/api/password/**", "/api/ai/**").permitAll()

                        // Các API còn lại yêu cầu vai trò ADMIN hoặc STAFF
                        .anyRequest().hasAnyRole("ADMIN", "STAFF")
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                        )
                );
        return http.build();
    }

    /**
     * Cấu hình bảo mật cho giao diện web thông thường và form đăng nhập.
     * Được xử lý sau cấu hình API (@Order(2)).
     */
    @Bean
    @Order(2)
    public SecurityFilterChain formLoginFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Hợp nhất TẤT CẢ các đường dẫn công khai vào một nơi duy nhất
                        .requestMatchers(
                                // Giữ nguyên các đường dẫn cũ
                                "/auth/**", "/", "/home/**", "/search", "/about", "/contact", "/categories",
                                "/books/details/**", "/stringee-webhook",
                                "/css/**", "/js/**", "/videos/**", "/templates/**", "/uploads/**",

                                // === THÊM CÁC ĐƯỜNG DẪN API CÔNG KHAI VÀO ĐÂY ===
                                "/api/password/**",
                                "/api/ai/**"
                        ).permitAll()
                        // Phân quyền chặt chẽ cho các vai trò
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/staff/**").hasAnyRole("ADMIN", "STAFF")
                        .requestMatchers("/reader/**").hasRole("READER")
                        // Tất cả các yêu cầu khác phải được xác thực
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/auth/login")
                        .loginProcessingUrl("/auth/login")
                        .successHandler(loginSuccessHandler)
                        .failureUrl("/auth/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .logoutSuccessUrl("/auth/login?logout")
                        .permitAll()
                );

        return http.build();
    }

}