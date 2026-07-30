package com.millenniumitesp.productinventoryservice.exception;

import java.util.UUID;

public class AuthExceptions {

    private AuthExceptions() {}

    public static class InvalidCredentials extends RuntimeException {
        public InvalidCredentials() {
            super("Invalid username or password");
        }
    }

    public static class UserNotFound extends RuntimeException {
        public UserNotFound(UUID id) {
            super("User not found with id: " + id);
        }
    }

    public static class UsernameAlreadyExists extends RuntimeException {
        public UsernameAlreadyExists(String username) {
            super("Username '" + username + "' is already taken");
        }
    }

    public static class InvalidRefreshToken extends RuntimeException {
        public InvalidRefreshToken() {
            super("Refresh token is invalid, expired, or has been revoked");
        }
    }

    public static class TokenReuseDetected extends RuntimeException {
        public TokenReuseDetected() {
            super("A previously used refresh token was reused. All sessions for this account have been revoked.");
        }
    }
}