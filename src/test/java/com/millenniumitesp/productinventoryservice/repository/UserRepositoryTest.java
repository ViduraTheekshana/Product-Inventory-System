package com.millenniumitesp.productinventoryservice.repository;

import com.millenniumitesp.productinventoryservice.entity.User;
import com.millenniumitesp.productinventoryservice.enums.Role;
import com.millenniumitesp.productinventoryservice.enums.UserStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    private final UserRepository userRepository;

    UserRepositoryTest(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Test
    void existsByUsername_shouldReturnTrue_whenUserExists() {
        userRepository.save(User.builder()
                .username("admin2").password("hashed").role(Role.ADMIN).status(UserStatus.ACTIVE).build());

        assertTrue(userRepository.existsByUsername("admin2"));
    }

    @Test
    void findAllByStatusNot_shouldExcludeDeletedUsers() {
        userRepository.save(User.builder()
                .username("active1").password("h").role(Role.VIEWER).status(UserStatus.ACTIVE).build());
        userRepository.save(User.builder()
                .username("deleted1").password("h").role(Role.VIEWER).status(UserStatus.DELETED).build());

        var page = userRepository.findAllByStatusNot(UserStatus.DELETED, PageRequest.of(0, 10));

        assertTrue(page.getContent().stream().noneMatch(u -> u.getUsername().equals("deleted1")));
    }

    @Test
    void findByIdAndStatusNot_shouldReturnEmpty_whenUserIsDeleted() {
        User saved = userRepository.save(User.builder()
                .username("gone").password("h").role(Role.VIEWER).status(UserStatus.DELETED).build());

        Optional<User> result = userRepository.findByIdAndStatusNot(saved.getId(), UserStatus.DELETED);

        assertTrue(result.isEmpty());
    }

    @Test
    void save_shouldThrowConstraintViolation_whenRoleIsNull() {
        User invalid = User.builder()
                .username("broken").password("h").status(UserStatus.ACTIVE).build(); // role missing

        assertThrows(DataIntegrityViolationException.class,
                () -> userRepository.saveAndFlush(invalid));
    }
}