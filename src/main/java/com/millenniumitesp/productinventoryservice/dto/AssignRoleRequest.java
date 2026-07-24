package com.millenniumitesp.productinventoryservice.dto;

import com.millenniumitesp.productinventoryservice.enums.Role;
import jakarta.validation.constraints.NotNull;

public record AssignRoleRequest(
        @NotNull(message = "role is required") Role role
) {}