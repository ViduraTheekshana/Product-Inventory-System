package com.millenniumitesp.productinventoryservice.dto;

import com.millenniumitesp.productinventoryservice.entity.User;
import com.millenniumitesp.productinventoryservice.enums.Role;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UserResponse(UUID id, String username, Role role) {
    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }
}