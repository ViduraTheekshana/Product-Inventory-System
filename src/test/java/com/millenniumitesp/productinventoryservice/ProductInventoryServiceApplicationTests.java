package com.millenniumitesp.productinventoryservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

// A genuine full-application smoke test - every bean, every layer,
// wired together for real. Unlike our slice tests, this needs a real
// (temporary) database, exactly like ProductRepositoryTest.
@SpringBootTest
@Testcontainers
class ProductInventoryServiceApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // The app also needs these to boot - same secrets our real
        // .env files provide, given real, valid placeholder values here.
        registry.add("jwt.secret", () -> "test-secret-key-for-application-context-smoke-test-only-1234567890");
        registry.add("jwt.refresh-secret", () -> "test-refresh-secret-key-for-smoke-test-only-0987654321abcdef");
        registry.add("admin.bootstrap-password", () -> "test-admin-password");
    }

    @Test
    void contextLoads() {
        // Intentionally empty - if the entire application context
        // fails to start for ANY reason (a missing bean, bad config,
        // wiring mismatch anywhere), this test fails automatically.
        // Passing IS the assertion.
    }
}