package com.millenniumitesp.productinventoryservice.config;

import com.millenniumitesp.productinventoryservice.entity.User;
import com.millenniumitesp.productinventoryservice.enums.Role;
import com.millenniumitesp.productinventoryservice.enums.UserStatus;
import com.millenniumitesp.productinventoryservice.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String bootstrapPassword;

    public AdminSeeder(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       @Value("${admin.bootstrap-password}") String bootstrapPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapPassword = bootstrapPassword;
    }

    @Override
    public void run(String... args) {
        boolean adminExists = userRepository.findByUsername("admin").isPresent();

        if (!adminExists) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode(bootstrapPassword))
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();

            userRepository.save(admin);
            log.warn("Bootstrap admin account created - username: 'admin'. " +
                    "Password was read from configuration, not hardcoded. CHANGE IT after first login.");
        } else {
            log.info("Admin account already exists - skipping seed.");
        }
    }
}