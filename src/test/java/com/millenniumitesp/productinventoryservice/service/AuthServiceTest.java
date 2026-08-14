package com.millenniumitesp.productinventoryservice.service;

import com.millenniumitesp.productinventoryservice.dto.*;
import com.millenniumitesp.productinventoryservice.entity.RefreshToken;
import com.millenniumitesp.productinventoryservice.entity.User;
import com.millenniumitesp.productinventoryservice.enums.Role;
import com.millenniumitesp.productinventoryservice.enums.UserStatus;
import com.millenniumitesp.productinventoryservice.exception.AuthExceptions;
import com.millenniumitesp.productinventoryservice.repository.RefreshTokenRepository;
import com.millenniumitesp.productinventoryservice.repository.UserRepository;
import com.millenniumitesp.productinventoryservice.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private Authentication authenticationResult;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, refreshTokenRepository, jwtService, authenticationManager);
    }

    @Test
    void login_shouldSucceed_whenCredentialsValid() {
        UUID userId = UUID.randomUUID();
        var principal = org.springframework.security.core.userdetails.User
                .withUsername("admin").password("hashed").authorities("ROLE_ADMIN").build();
        User user = User.builder().id(userId).username("admin").password("hashed")
                .role(Role.ADMIN).status(UserStatus.ACTIVE).build();

        when(authenticationManager.authenticate(any())).thenReturn(authenticationResult);
        when(authenticationResult.getPrincipal()).thenReturn(principal);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken("admin", "ADMIN")).thenReturn("access-token");
        when(jwtService.generateRefreshToken(eq("admin"), anyString(), any(Instant.class))).thenReturn("refresh-token");

        LoginResponse response = authService.login(new LoginRequest("admin", "admin123"));

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void login_shouldThrowInvalidCredentials_whenAuthenticationFails() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(AuthExceptions.InvalidCredentials.class,
                () -> authService.login(new LoginRequest("admin", "wrong")));
    }

    @Test
    void refresh_shouldIssueNewTokens_whenTokenValid() {
        UUID userId = UUID.randomUUID();
        RefreshToken stored = RefreshToken.builder()
                .jti("jti-1").userId(userId).expiresAt(Instant.now().plusSeconds(3600)).revoked(false).build();
        User user = User.builder().id(userId).username("admin").password("hashed")
                .role(Role.ADMIN).status(UserStatus.ACTIVE).build();

        when(jwtService.isRefreshTokenValid("old-token")).thenReturn(true);
        when(jwtService.extractRefreshJti("old-token")).thenReturn("jti-1");
        when(refreshTokenRepository.findByJti("jti-1")).thenReturn(Optional.of(stored));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken("admin", "ADMIN")).thenReturn("new-access");
        when(jwtService.generateRefreshToken(eq("admin"), anyString(), any(Instant.class))).thenReturn("new-refresh");

        LoginResponse response = authService.refresh(new RefreshRequest("old-token"));

        assertEquals("new-access", response.accessToken());
        assertTrue(stored.isRevoked());
    }

    @Test
    void refresh_shouldThrowTokenReuseDetected_whenTokenAlreadyRevoked() {
        UUID userId = UUID.randomUUID();
        RefreshToken stored = RefreshToken.builder()
                .jti("jti-1").userId(userId).expiresAt(Instant.now().plusSeconds(3600)).revoked(true).build();

        when(jwtService.isRefreshTokenValid("stale-token")).thenReturn(true);
        when(jwtService.extractRefreshJti("stale-token")).thenReturn("jti-1");
        when(refreshTokenRepository.findByJti("jti-1")).thenReturn(Optional.of(stored));
        when(refreshTokenRepository.findAllByUserId(userId)).thenReturn(List.of(stored));

        assertThrows(AuthExceptions.TokenReuseDetected.class,
                () -> authService.refresh(new RefreshRequest("stale-token")));

        verify(refreshTokenRepository).saveAll(anyList());
    }

    @Test
    void refresh_shouldThrowInvalidRefreshToken_whenSignatureInvalid() {
        when(jwtService.isRefreshTokenValid("garbage")).thenReturn(false);

        assertThrows(AuthExceptions.InvalidRefreshToken.class,
                () -> authService.refresh(new RefreshRequest("garbage")));

        verify(refreshTokenRepository, never()).findByJti(anyString());
    }

    @Test
    void logout_shouldRevokeToken_whenValid() {
        RefreshToken stored = RefreshToken.builder().jti("jti-1").userId(UUID.randomUUID())
                .expiresAt(Instant.now().plusSeconds(3600)).revoked(false).build();

        when(jwtService.isRefreshTokenValid("token")).thenReturn(true);
        when(jwtService.extractRefreshJti("token")).thenReturn("jti-1");
        when(refreshTokenRepository.findByJti("jti-1")).thenReturn(Optional.of(stored));

        authService.logout(new LogoutRequest("token"));

        assertTrue(stored.isRevoked());
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    void logoutAllSessions_shouldRevokeEveryTokenForUser() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).username("admin").password("h")
                .role(Role.ADMIN).status(UserStatus.ACTIVE).build();
        RefreshToken token1 = RefreshToken.builder().jti("a").userId(userId)
                .expiresAt(Instant.now().plusSeconds(3600)).revoked(false).build();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(refreshTokenRepository.findAllByUserId(userId)).thenReturn(List.of(token1));

        authService.logoutAllSessions("admin");

        assertTrue(token1.isRevoked());
    }
}