package com.millenniumitesp.productinventoryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.millenniumitesp.productinventoryservice.dto.LoginRequest;
import com.millenniumitesp.productinventoryservice.dto.LoginResponse;
import com.millenniumitesp.productinventoryservice.dto.LogoutRequest;
import com.millenniumitesp.productinventoryservice.dto.RefreshRequest;
import com.millenniumitesp.productinventoryservice.exception.AuthExceptions;
import com.millenniumitesp.productinventoryservice.security.JwtAuthFilter;
import com.millenniumitesp.productinventoryservice.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = { JwtAuthFilter.class })
)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthService authService;

    AuthControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void login_shouldReturn200_withTokens() throws Exception {
        LoginResponse response = new LoginResponse("access-token", "refresh-token");
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"));
    }

    @Test
    void login_shouldReturn401_whenCredentialsInvalid() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new AuthExceptions.InvalidCredentials());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin", "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void refresh_shouldReturn200_withNewTokens() throws Exception {
        LoginResponse response = new LoginResponse("new-access", "new-refresh");
        when(authService.refresh(any(RefreshRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest("old-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access"));
    }

    @Test
    void refresh_shouldReturn401_whenTokenReused() throws Exception {
        when(authService.refresh(any(RefreshRequest.class)))
                .thenThrow(new AuthExceptions.TokenReuseDetected());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RefreshRequest("stale-token"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_shouldReturn204() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LogoutRequest("token"))))
                .andExpect(status().isNoContent());

        verify(authService).logout(any(LogoutRequest.class));
    }

    @Test
    void logoutAll_shouldReturn204_andUseAuthenticatedUsername() throws Exception {
        // Manually place a fake authenticated principal into the security
        // context, simulating what JwtAuthFilter would normally have set -
        // since we've excluded that filter from this slice entirely.
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin", null)
        );

        try {
            mockMvc.perform(post("/api/v1/auth/logout-all"))
                    .andExpect(status().isNoContent());

            verify(authService).logoutAllSessions("admin");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}