package com.millenniumitesp.productinventoryservice.controller;

import com.millenniumitesp.productinventoryservice.dto.LoginRequest;
import com.millenniumitesp.productinventoryservice.dto.LoginResponse;
import com.millenniumitesp.productinventoryservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController implements AuthApi {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}