package com.millenniumitesp.productinventoryservice.service;

import com.millenniumitesp.productinventoryservice.dto.AssignRoleRequest;
import com.millenniumitesp.productinventoryservice.dto.CreateUserRequest;
import com.millenniumitesp.productinventoryservice.dto.UserResponse;
import com.millenniumitesp.productinventoryservice.entity.User;
import com.millenniumitesp.productinventoryservice.enums.UserStatus;
import com.millenniumitesp.productinventoryservice.exception.AuthExceptions;
import com.millenniumitesp.productinventoryservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new AuthExceptions.UsernameAlreadyExists(request.username());
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .status(UserStatus.ACTIVE)
                .build();

        return UserResponse.fromEntity(userRepository.save(user));
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    public UserResponse assignRole(UUID id, AssignRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AuthExceptions.UserNotFound(id));

        user.setRole(request.role());
        return UserResponse.fromEntity(userRepository.save(user));
    }

    public UserResponse suspendUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AuthExceptions.UserNotFound(id));

        user.setStatus(UserStatus.INACTIVE);
        return UserResponse.fromEntity(userRepository.save(user));
    }

    public UserResponse reactivateUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AuthExceptions.UserNotFound(id));

        user.setStatus(UserStatus.ACTIVE);
        return UserResponse.fromEntity(userRepository.save(user));
    }

    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AuthExceptions.UserNotFound(id));

        user.setStatus(UserStatus.DELETED);
        userRepository.save(user);
    }
}