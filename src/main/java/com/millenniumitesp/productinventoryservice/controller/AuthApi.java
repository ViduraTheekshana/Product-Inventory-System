package com.millenniumitesp.productinventoryservice.controller;

import com.millenniumitesp.productinventoryservice.dto.LoginRequest;
import com.millenniumitesp.productinventoryservice.dto.LoginResponse;
import com.millenniumitesp.productinventoryservice.dto.LogoutRequest;
import com.millenniumitesp.productinventoryservice.dto.RefreshRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "Login, token refresh, and logout")
public interface AuthApi {

    @Operation(summary = "Log in", description = "Returns a short-lived access token and a longer-lived refresh token.")
    @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "401", description = "Invalid username or password",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    LoginResponse login(@Valid @RequestBody LoginRequest request);

    @Operation(summary = "Refresh an access token",
            description = "Rotates the refresh token: the old one is invalidated, a new one is issued. Reuse of an already-rotated token revokes every session for that user.")
    @ApiResponse(responseCode = "200", description = "New access and refresh token issued",
            content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "401", description = "Refresh token invalid, expired, or reused (all sessions revoked)",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    LoginResponse refresh(@Valid @RequestBody RefreshRequest request);

    @Operation(summary = "Log out this session", description = "Revokes only the provided refresh token.")
    @ApiResponse(responseCode = "204", description = "Logged out successfully")
    void logout(@Valid @RequestBody LogoutRequest request);

    @Operation(summary = "Log out everywhere",
            description = "Revokes every refresh token for the currently authenticated user - all devices, all sessions.")
    @ApiResponse(responseCode = "204", description = "All sessions revoked")
    void logoutAll();
}