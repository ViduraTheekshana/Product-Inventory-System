package com.millenniumitesp.productinventoryservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

// Plain unit test - GlobalExceptionHandler needs no Spring context at
// all to call its methods directly. We fake just enough of
// HttpServletRequest to satisfy request.getMethod()/getRequestURI().
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest request = mock(HttpServletRequest.class);

    @Test
    void handleException_shouldReturn404_forProductNotFound() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/products/1");

        ProblemDetail result = handler.handleException(new ProductExceptions.NotFound(1L), request);

        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatus());
        assertEquals("Product Not Found", result.getTitle());
    }

    @Test
    void handleException_shouldReturn409_forDuplicateSku() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/products");

        ProblemDetail result = handler.handleException(new ProductExceptions.DuplicateSku("SKU-1"), request);

        assertEquals(HttpStatus.CONFLICT.value(), result.getStatus());
    }

    @Test
    void handleException_shouldReturn400_forStockLimitExceeded() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/products");

        ProblemDetail result = handler.handleException(
                new ProductExceptions.StockLimitExceeded(999999, 0, 100000), request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
    }

    @Test
    void handleException_shouldReturn401_forTokenReuseDetected() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/auth/refresh");

        ProblemDetail result = handler.handleException(new AuthExceptions.TokenReuseDetected(), request);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getStatus());
        assertEquals("Session Revoked", result.getTitle());
    }

    @Test
    void handleException_shouldReturn401_forInvalidRefreshToken() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/auth/refresh");

        ProblemDetail result = handler.handleException(new AuthExceptions.InvalidRefreshToken(), request);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getStatus());
    }

    @Test
    void handleException_shouldReturn409_forOptimisticLockingFailure() {
        when(request.getMethod()).thenReturn("PATCH");
        when(request.getRequestURI()).thenReturn("/api/v1/products/1");

        ProblemDetail result = handler.handleException(
                new ObjectOptimisticLockingFailureException(Object.class, 1L), request);

        assertEquals(HttpStatus.CONFLICT.value(), result.getStatus());
        assertEquals("Concurrent Update Conflict", result.getTitle());
    }

    @Test
    void handleException_shouldReturn409_forDataIntegrityViolation() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/products");

        ProblemDetail result = handler.handleException(
                new DataIntegrityViolationException("duplicate key"), request);

        assertEquals(HttpStatus.CONFLICT.value(), result.getStatus());
    }

    @Test
    void handleException_shouldReturn500_forUnexpectedException() {
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/api/v1/products/1");

        ProblemDetail result = handler.handleException(new RuntimeException("boom"), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
        assertEquals("Internal Server Error", result.getTitle());
    }
}