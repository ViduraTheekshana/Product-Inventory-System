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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                       JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authRequest = new UsernamePasswordAuthenticationToken(request.username(), request.password());
        Authentication authResult;

        try {
            authResult = authenticationManager.authenticate(authRequest);
        } catch (AuthenticationException e) {
            throw new AuthExceptions.InvalidCredentials();
        }

        UserDetails userDetails = (UserDetails) authResult.getPrincipal();
        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(AuthExceptions.InvalidCredentials::new);

        revokeAllTokensForUser(user.getId());

        String accessToken = jwtService.generateAccessToken(userDetails.getUsername(), role);
        String refreshToken = issueRefreshToken(user, Instant.now().plusSeconds(60L * 60 * 24 * 7));

        return new LoginResponse(accessToken, refreshToken);
    }

    public LoginResponse refresh(RefreshRequest request) {
        String presentedToken = request.refreshToken();

        if (!jwtService.isRefreshTokenValid(presentedToken)) {
            throw new AuthExceptions.InvalidRefreshToken();
        }

        String jti = jwtService.extractRefreshJti(presentedToken);
        RefreshToken stored = refreshTokenRepository.findByJti(jti)
                .orElseThrow(AuthExceptions.InvalidRefreshToken::new);

        if (stored.isRevoked()) {
            revokeAllTokensForUser(stored.getUserId());
            throw new AuthExceptions.TokenReuseDetected();
        }

        if (stored.isExpired()) {
            throw new AuthExceptions.InvalidRefreshToken();
        }

        User user = userRepository.findById(stored.getUserId())
                .orElseThrow(() -> new AuthExceptions.UserNotFound(stored.getUserId()));

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        String newRefreshToken = issueRefreshToken(user, stored.getExpiresAt());
        String newAccessToken = jwtService.generateAccessToken(user.getUsername(), user.getRole().name());

        return new LoginResponse(newAccessToken, newRefreshToken);
    }

    public void logout(LogoutRequest request) {
        if (!jwtService.isRefreshTokenValid(request.refreshToken())) {
            return;
        }
        String jti = jwtService.extractRefreshJti(request.refreshToken());
        refreshTokenRepository.findByJti(jti).ifPresent(token -> {
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

    private String issueRefreshToken(User user, Instant expiresAt) {
        String jti = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .jti(jti)
                .userId(user.getId())
                .expiresAt(expiresAt)
                .build();
        refreshTokenRepository.save(refreshToken);

        return jwtService.generateRefreshToken(user.getUsername(), jti, expiresAt);
    }
}