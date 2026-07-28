package com.millenniumitesp.productinventoryservice.config;

import com.millenniumitesp.productinventoryservice.enums.Role;
import com.millenniumitesp.productinventoryservice.security.JwtAccessDeniedHandler;
import com.millenniumitesp.productinventoryservice.security.JwtAuthFilter;
import com.millenniumitesp.productinventoryservice.security.JwtAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private static final String PRODUCTS_PATH = "/api/v1/products/**";

    private final JwtAuthFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          JwtAuthenticationEntryPoint authenticationEntryPoint,
                          JwtAccessDeniedHandler accessDeniedHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @SuppressWarnings("java:S112") // HttpSecurity.build() throws checked Exception - standard Spring Security signature
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF is unnecessary here: this API is stateless JWT-based
                // (Authorization header, not cookies), so there's no browser
                // session for a malicious site to forge a request against.
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        .requestMatchers(HttpMethod.GET, PRODUCTS_PATH).authenticated()
                        .requestMatchers(HttpMethod.POST, PRODUCTS_PATH).hasAnyRole(Role.MANAGER.name(), Role.ADMIN.name())
                        .requestMatchers(HttpMethod.PATCH, PRODUCTS_PATH).hasAnyRole(Role.MANAGER.name(), Role.ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, PRODUCTS_PATH).hasAnyRole(Role.MANAGER.name(), Role.ADMIN.name())

                        .requestMatchers("/api/v1/users/**").hasRole(Role.ADMIN.name())

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}