package com.millenniumitesp.productinventoryservice.controller;

import com.millenniumitesp.productinventoryservice.dto.AssignRoleRequest;
import com.millenniumitesp.productinventoryservice.dto.CreateUserRequest;
import com.millenniumitesp.productinventoryservice.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@Tag(name = "Users", description = "User management - ADMIN only")
public interface UserApi {

    @Operation(summary = "Create a new user",
            description = "Creates a login account with a specific role. Restricted to ADMIN.")
    @ApiResponse(responseCode = "201", description = "User created",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "409", description = "Username already taken",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    UserResponse createUser(@Valid @RequestBody CreateUserRequest request);

    @Operation(summary = "Assign a role to an existing user",
            description = "Changes a user's role. Restricted to ADMIN.")
    @ApiResponse(responseCode = "200", description = "Role updated",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "404", description = "No user exists with the given id",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    UserResponse assignRole(
            @Parameter(description = "The user's unique id") @PathVariable UUID userId,
            @Valid @RequestBody AssignRoleRequest request
    );
}