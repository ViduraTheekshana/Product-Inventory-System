package com.millenniumitesp.productinventoryservice.config;

import com.millenniumitesp.productinventoryservice.security.JwtAuthenticationProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * Deliberately separate from SecurityConfig. This class has NO
 * constructor dependencies at all, so Spring can build it freely,
 * early, without needing anything else first. If these beans lived
 * inside SecurityConfig instead, building AuthenticationManager would
 * require first building a SecurityConfig instance - which itself
 * depends on JwtAuthFilter, which depends on AuthenticationManager -
 * a circular reference. Splitting the file breaks that cycle.
 */
@Configuration
public class AuthenticationBeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(UserDetailsService userDetailsService,
                                                               PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(DaoAuthenticationProvider daoAuthenticationProvider,
                                                       JwtAuthenticationProvider jwtAuthenticationProvider) {
        return new ProviderManager(List.of(daoAuthenticationProvider, jwtAuthenticationProvider));
    }
}