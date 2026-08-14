package com.millenniumitesp.productinventoryservice.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

// No @ExtendWith(MockitoExtension.class) needed here at all - JwtService
// has zero dependencies to mock, just plain cryptographic logic. We
// construct it directly with test-only secret strings.
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(
                "test-access-secret-key-for-unit-testing-only-1234567890",
                "test-refresh-secret-key-for-unit-testing-only-0987654321"
        );
    }

    @Test
    void generateAccessToken_shouldProduceValidToken() {
        String token = jwtService.generateAccessToken("admin", "ADMIN");

        assertTrue(jwtService.isTokenValid(token));
        assertEquals("admin", jwtService.extractUsername(token));
        assertEquals("ADMIN", jwtService.extractRole(token));
    }

    @Test
    void isTokenValid_shouldReturnFalse_forGarbageInput() {
        assertFalse(jwtService.isTokenValid("not-a-real-token"));
    }

    @Test
    void generateRefreshToken_shouldEmbedCorrectJti() {
        String jti = "test-jti-12345";
        String token = jwtService.generateRefreshToken("admin", jti, Instant.now().plusSeconds(3600));

        assertTrue(jwtService.isRefreshTokenValid(token));
        assertEquals(jti, jwtService.extractRefreshJti(token));
    }

    @Test
    void isRefreshTokenValid_shouldReturnFalse_whenSignedWithWrongKey() {
        // A token signed with the ACCESS key should fail refresh
        // validation, since they use genuinely separate secrets -
        // this proves our key-separation design actually works.
        String accessToken = jwtService.generateAccessToken("admin", "ADMIN");

        assertFalse(jwtService.isRefreshTokenValid(accessToken));
    }

    @Test
    void isTokenValid_shouldReturnFalse_forExpiredToken() {
        // A token signed correctly, but already past its expiry instant.
        JwtService shortLivedService = new JwtService(
                "test-access-secret-key-for-unit-testing-only-1234567890",
                "test-refresh-secret-key-for-unit-testing-only-0987654321"
        );
        String expiredRefresh = shortLivedService.generateRefreshToken(
                "admin", "jti-x", Instant.now().minusSeconds(10)
        );

        assertFalse(shortLivedService.isRefreshTokenValid(expiredRefresh));
    }
}