package com.millenniumitesp.productinventoryservice.dto;

import com.millenniumitesp.productinventoryservice.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUserRequest(
        @NotBlank(message = "username is required") String username,
        @NotBlank(message = "password is required") String password,
        @NotNull(message = "role is required") Role role
) {}