package com.millenniumitesp.productinventoryservice.dto;

import com.millenniumitesp.productinventoryservice.entity.User;
import com.millenniumitesp.productinventoryservice.enums.Role;
import java.util.UUID;

public record UserResponse(UUID id, String username, Role role) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole());
    }
}