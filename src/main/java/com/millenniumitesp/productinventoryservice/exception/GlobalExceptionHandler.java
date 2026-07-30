package com.millenniumitesp.productinventoryservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Stays separate: overrides a specific parent method with its own
    // required signature, not a Freeform @ExceptionHandler.
    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> Objects.requireNonNullElse(fe.getDefaultMessage(), "Invalid value"),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new));

        log.warn("Validation failed: {}", fieldErrors);

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "One or more fields are invalid.");
        problem.setTitle("Validation Failed");
        problem.setProperty("errors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    /**
     * Every other exception type funnels through this single method.
     * Pattern-matching switch (Java 21+) dispatches on the exception's
     * runtime type - equivalent to a chain of instanceof checks, but
     * exhaustive, type-safe, and far less repetitive than one
     * @ExceptionHandler method per exception type.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception ex, HttpServletRequest request) {
        return switch (ex) {
            case ProductExceptions.NotFound e -> {
                log.warn(e.getMessage());
                yield buildProblem(HttpStatus.NOT_FOUND, "Product Not Found", e.getMessage());
            }
            case ProductExceptions.DuplicateSku e -> {
                log.warn(e.getMessage());
                yield buildProblem(HttpStatus.CONFLICT, "Duplicate SKU", e.getMessage());
            }
            case ProductExceptions.StockLimitExceeded e -> {
                log.warn(e.getMessage());
                yield buildProblem(HttpStatus.BAD_REQUEST, "Stock Limit Exceeded", e.getMessage());
            }
            case ProductExceptions.InvalidStatusTransition e -> {
                log.warn(e.getMessage());
                yield buildProblem(HttpStatus.BAD_REQUEST, "Invalid Status Transition", e.getMessage());
            }
            case ObjectOptimisticLockingFailureException e -> {
                log.warn("Optimistic locking conflict: {}", e.getMessage());
                yield buildProblem(HttpStatus.CONFLICT, "Concurrent Update Conflict",
                        "This product was updated by someone else at the same time. Please retry.");
            }
            case DataIntegrityViolationException e -> {
                log.warn("Data integrity violation on {} {}: {}", request.getMethod(), request.getRequestURI(), e.getMessage());
                yield buildProblem(HttpStatus.CONFLICT, "Data Conflict", "This request conflicts with existing data.");
            }
            case ConstraintViolationException e -> {
                log.warn("Constraint violation: {}", e.getMessage());
                yield buildProblem(HttpStatus.BAD_REQUEST, "Invalid Request", e.getMessage());
            }
            case AuthExceptions.InvalidCredentials e -> {
                log.warn("Failed login attempt");
                yield buildProblem(HttpStatus.UNAUTHORIZED, "Authentication Failed", e.getMessage());
            }
            case AuthExceptions.UserNotFound e -> {
                log.warn(e.getMessage());
                yield buildProblem(HttpStatus.NOT_FOUND, "User Not Found", e.getMessage());
            }
            case AuthExceptions.UsernameAlreadyExists e -> {
                log.warn(e.getMessage());
                yield buildProblem(HttpStatus.CONFLICT, "Username Taken", e.getMessage());
            }
            case AuthExceptions.InvalidRefreshToken e -> {
                log.warn(e.getMessage());
                yield buildProblem(HttpStatus.UNAUTHORIZED, "Invalid Refresh Token", e.getMessage());
            }
            case AuthExceptions.TokenReuseDetected e -> {
                log.warn("Token reuse detected - possible account compromise");
                yield buildProblem(HttpStatus.UNAUTHORIZED, "Session Revoked", e.getMessage());
            }
            default -> {
                log.error("Unhandled exception on {} {}", request.getMethod(), request.getRequestURI(), ex);
                yield buildProblem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred.");
            }
        };
    }

    private ProblemDetail buildProblem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}