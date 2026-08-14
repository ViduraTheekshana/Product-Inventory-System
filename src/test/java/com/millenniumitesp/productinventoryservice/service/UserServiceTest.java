package com.millenniumitesp.productinventoryservice.service;

import com.millenniumitesp.productinventoryservice.dto.AssignRoleRequest;
import com.millenniumitesp.productinventoryservice.dto.CreateUserRequest;
import com.millenniumitesp.productinventoryservice.dto.UserResponse;
import com.millenniumitesp.productinventoryservice.entity.User;
import com.millenniumitesp.productinventoryservice.enums.Role;
import com.millenniumitesp.productinventoryservice.enums.UserStatus;
import com.millenniumitesp.productinventoryservice.exception.AuthExceptions;
import com.millenniumitesp.productinventoryservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    // Built by hand in setUp, not @InjectMocks - explicit, no reflection magic.
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Test
    void createUser_shouldSucceed_whenUsernameIsNew() {
        CreateUserRequest request = new CreateUserRequest("manager1", "pass123", Role.MANAGER);
        User saved = User.builder()
                .id(UUID.randomUUID()).username("manager1").password("hashed")
                .role(Role.MANAGER).status(UserStatus.ACTIVE).build();

        when(userRepository.existsByUsername("manager1")).thenReturn(false);
        when(passwordEncoder.encode("pass123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.createUser(request);

        assertEquals("manager1", response.username());
        assertEquals(Role.MANAGER, response.role());
    }

    @Test
    void createUser_shouldThrowUsernameAlreadyExists_whenDuplicate() {
        CreateUserRequest request = new CreateUserRequest("manager1", "pass123", Role.MANAGER);
        when(userRepository.existsByUsername("manager1")).thenReturn(true);

        assertThrows(AuthExceptions.UsernameAlreadyExists.class, () -> userService.createUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void assignRole_shouldUpdateRole_whenUserIsActive() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id).username("viewer1").password("hashed")
                .role(Role.VIEWER).status(UserStatus.ACTIVE).build();

        when(userRepository.findByIdAndStatusNot(id, UserStatus.DELETED)).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse response = userService.assignRole(id, new AssignRoleRequest(Role.MANAGER));

        assertEquals(Role.MANAGER, response.role());
    }

    @Test
    void assignRole_shouldThrowUserNotFound_whenUserIsDeletedOrMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findByIdAndStatusNot(id, UserStatus.DELETED)).thenReturn(java.util.Optional.empty());

        assertThrows(AuthExceptions.UserNotFound.class,
                () -> userService.assignRole(id, new AssignRoleRequest(Role.ADMIN)));
    }

    @Test
    void suspendUser_shouldSetStatusInactive() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id).username("v").password("h").role(Role.VIEWER).status(UserStatus.ACTIVE).build();

        when(userRepository.findByIdAndStatusNot(id, UserStatus.DELETED)).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.suspendUser(id);

        assertEquals(UserStatus.INACTIVE, response.status());
    }

    @Test
    void reactivateUser_shouldSetStatusActive() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id).username("v").password("h").role(Role.VIEWER).status(UserStatus.INACTIVE).build();

        when(userRepository.findByIdAndStatusNot(id, UserStatus.DELETED)).thenReturn(java.util.Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserResponse response = userService.reactivateUser(id);

        assertEquals(UserStatus.ACTIVE, response.status());
    }

    @Test
    void deleteUser_shouldSetStatusDeleted() {
        UUID id = UUID.randomUUID();
        User user = User.builder()
                .id(id).username("v").password("h").role(Role.VIEWER).status(UserStatus.ACTIVE).build();

        when(userRepository.findByIdAndStatusNot(id, UserStatus.DELETED)).thenReturn(java.util.Optional.of(user));

        userService.deleteUser(id);

        assertEquals(UserStatus.DELETED, user.getStatus());
        verify(userRepository).save(user);
    }
}