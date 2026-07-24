package com.millenniumitesp.productinventoryservice.service;

import com.millenniumitesp.productinventoryservice.dto.LoginRequest;
import com.millenniumitesp.productinventoryservice.dto.LoginResponse;
import com.millenniumitesp.productinventoryservice.entity.User;
import com.millenniumitesp.productinventoryservice.exception.AuthExceptions;
import com.millenniumitesp.productinventoryservice.repository.UserRepository;
import com.millenniumitesp.productinventoryservice.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/*
use for Login only
***/
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(AuthExceptions.InvalidCredentials::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthExceptions.InvalidCredentials();
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        return new LoginResponse(token);
    }
}