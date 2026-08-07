package com.millenniumitesp.productinventoryservice.controller;

import com.millenniumitesp.productinventoryservice.config.PaginationProperties;
import com.millenniumitesp.productinventoryservice.dto.AssignRoleRequest;
import com.millenniumitesp.productinventoryservice.dto.CreateUserRequest;
import com.millenniumitesp.productinventoryservice.dto.UserResponse;
import com.millenniumitesp.productinventoryservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController implements UserApi {

    private final UserService userService;
    private final PaginationProperties paginationProperties;

    public UserController(UserService userService, PaginationProperties paginationProperties) {
        this.userService = userService;
        this.paginationProperties = paginationProperties;
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @Override
    @GetMapping
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        // Same exact pattern as ProductController: if the caller never
        // specified ?size=, Spring's own default of 20 is what we're
        // seeing here - swap it for our configured, environment-specific
        // default instead.
        Pageable effectivePageable = pageable.getPageSize() == 20 && pageable.getSort().isUnsorted()
                ? PageRequest.of(pageable.getPageNumber(), paginationProperties.getDefaultSize())
                : pageable;

        return userService.getAllUsers(effectivePageable);
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