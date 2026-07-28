package com.millenniumitesp.productinventoryservice.controller;

import com.millenniumitesp.productinventoryservice.dto.AssignRoleRequest;
import com.millenniumitesp.productinventoryservice.dto.CreateUserRequest;
import com.millenniumitesp.productinventoryservice.dto.UserResponse;
import com.millenniumitesp.productinventoryservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController implements UserApi {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @Override
    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @Override
    @PatchMapping("/{userId}/role")
    public UserResponse assignRole(@PathVariable UUID userId, @Valid @RequestBody AssignRoleRequest request) {
        return userService.assignRole(userId, request);
    }

    @Override
    @PatchMapping("/{userId}/suspend")
    public UserResponse suspendUser(@PathVariable UUID userId) {
        return userService.suspendUser(userId);
    }

    @Override
    @PatchMapping("/{userId}/reactivate")
    public UserResponse reactivateUser(@PathVariable UUID userId) {
        return userService.reactivateUser(userId);
    }

    @Override
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);
    }
}