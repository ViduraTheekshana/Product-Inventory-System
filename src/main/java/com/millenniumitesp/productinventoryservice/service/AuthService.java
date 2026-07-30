package com.millenniumitesp.productinventoryservice.service;

import com.millenniumitesp.productinventoryservice.dto.LoginRequest;
import com.millenniumitesp.productinventoryservice.dto.LoginResponse;
import com.millenniumitesp.productinventoryservice.dto.LogoutRequest;
import com.millenniumitesp.productinventoryservice.dto.RefreshRequest;
import com.millenniumitesp.productinventoryservice.entity.RefreshToken;
import com.millenniumitesp.productinventoryservice.entity.User;
import com.millenniumitesp.productinventoryservice.exception.AuthExceptions;
import com.millenniumitesp.productinventoryservice.repository.RefreshTokenRepository;
import com.millenniumitesp.productinventoryservice.repository.UserRepository;
import com.millenniumitesp.productinventoryservice.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(AuthExceptions.InvalidCredentials::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthExceptions.InvalidCredentials();
        }

        String accessToken = jwtService.generateAccessToken(user.getUsername(), user.getRole().name());
        String refreshTokenValue = issueRefreshToken(user.getId(), Instant.now().plusSeconds(60L * 60 * 24 * 7));

        return new LoginResponse(accessToken, refreshTokenValue);
    }

    public LoginResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(AuthExceptions.InvalidRefreshToken::new);

        if (stored.isRevoked()) {
            // A stale, already-rotated-out token has resurfaced - treat
            // this as evidence of theft, not just an expired session.
            revokeAllTokensForUser(stored.getUserId());
            throw new AuthExceptions.TokenReuseDetected();
        }

        if (stored.isExpired()) {
            throw new AuthExceptions.InvalidRefreshToken();
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new AuthExceptions.UserNotFound(stored.getUserId()));

        // Rotate: this token is now spent, a new one takes its place.
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        // The cap carries forward unchanged - a session can never
        // silently extend itself past the original 7-day limit from login.
        String newRefreshValue = issueRefreshToken(user.getId(), stored.getExpiresAt());

        String newAccessToken = jwtService.generateAccessToken(user.getUsername(), user.getRole().name());
        return new LoginResponse(newAccessToken, newRefreshValue);
    }

    public void logout(LogoutRequest request) {
        refreshTokenRepository.findByToken(request.refreshToken())
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    public void logoutAllSessions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(AuthExceptions.InvalidCredentials::new);
        revokeAllTokensForUser(user.getId());
    }

    private void revokeAllTokensForUser(UUID userId) {
        List<RefreshToken> tokens = refreshTokenRepository.findAllByUserId(userId);
        tokens.forEach(t -> t.setRevoked(true));
        refreshTokenRepository.saveAll(tokens);
    }

    private String issueRefreshToken(UUID userId, Instant expiresAt) {
        String tokenValue = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(tokenValue)
                .userId(userId)
                .expiresAt(expiresAt)
                .build();

        refreshTokenRepository.save(refreshToken);
        return tokenValue;
    }
}