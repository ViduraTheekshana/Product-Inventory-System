package com.millenniumitesp.productinventoryservice.service;

import com.millenniumitesp.productinventoryservice.dto.AssignRoleRequest;
import com.millenniumitesp.productinventoryservice.dto.CreateUserRequest;
import com.millenniumitesp.productinventoryservice.dto.UserResponse;
import com.millenniumitesp.productinventoryservice.entity.User;
import com.millenniumitesp.productinventoryservice.exception.AuthExceptions;
import com.millenniumitesp.productinventoryservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
                .password(passwordEncoder.encode(request.password())) // hash before storing
                .role(request.role())
                .build();

        return UserResponse.fromEntity(userRepository.save(user));
    }

    public UserResponse assignRole(UUID userId, AssignRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthExceptions.UserNotFound(userId));

        user.setRole(request.role());
        return UserResponse.fromEntity(userRepository.save(user));
    }
}