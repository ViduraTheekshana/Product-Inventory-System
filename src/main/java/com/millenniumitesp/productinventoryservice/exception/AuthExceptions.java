package com.millenniumitesp.productinventoryservice.exception;

public class AuthExceptions {

    private AuthExceptions() {}

    public static class InvalidCredentials extends RuntimeException {
        public InvalidCredentials() {
            super("Invalid username or password");
        }
    }

    public static class UserNotFound extends RuntimeException {
        public UserNotFound(java.util.UUID id) {
            super("User not found with id: " + id);
        }
    }

    public static class UsernameAlreadyExists extends RuntimeException {
        public UsernameAlreadyExists(String username) {
            super("Username '" + username + "' is already taken");
        }
    }
}