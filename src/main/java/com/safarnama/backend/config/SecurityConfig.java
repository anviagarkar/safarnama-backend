package com.safarnama.backend.config;

import com.safarnama.backend.auth.JwtAuthFilter;
import com.safarnama.backend.auth.ResetPasswordRateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ResetPasswordRateLimitFilter resetPasswordRateLimitFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, ResetPasswordRateLimitFilter resetPasswordRateLimitFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.resetPasswordRateLimitFilter = resetPasswordRateLimitFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/signup", "/auth/login", "/auth/forgot-password", "/auth/reset-password").permitAll()
                        .anyRequest().authenticated()
                )
                // Rate limiter runs before the JWT filter since forgot/reset-password
                // are unauthenticated endpoints — this is what stops the 6-digit
                // reset code from being brute-forceable within its 1-hour expiry.
                .addFilterBefore(resetPasswordRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}