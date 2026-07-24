package com.millenniumitesp.productinventoryservice.controller;

import com.millenniumitesp.productinventoryservice.dto.LoginRequest;
import com.millenniumitesp.productinventoryservice.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Auth", description = "Login and token issuance")
public interface AuthApi {

    @Operation(summary = "Log in and receive a JWT",
            description = "Exchanges a username/password for a signed JWT, used as a Bearer token on all other endpoints.")
    @ApiResponse(responseCode = "200", description = "Login successful",
            content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "401", description = "Invalid username or password",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    LoginResponse login(@Valid @RequestBody LoginRequest request);
}