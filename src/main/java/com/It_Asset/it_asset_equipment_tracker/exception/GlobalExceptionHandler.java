
package com.It_Asset.it_asset_equipment_tracker.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.security.core.AuthenticationException;

import org.springframework.transaction.TransactionSystemException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Create a consistent JSON error response
    private Map<String, Object> createErrorResponse(
            HttpStatusCode status,
            String message,
            String path) {

        Map<String, Object> error = new LinkedHashMap<>();

        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", status.toString());
        error.put("message", message);
        error.put("path", path);

        return error;
    }

    // Handle resources that cannot be found (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.NOT_FOUND;

        return ResponseEntity.status(status)
                .body(createErrorResponse(
                        status,
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    // Handle validation errors (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": "
                                + error.getDefaultMessage())
                .findFirst()
                .orElse("Request validation failed");

        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status)
                .body(createErrorResponse(
                        status,
                        message,
                        request.getRequestURI()));
    }

    // Handle invalid JSON, request bodies, or enum values (400)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRequest(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status)
                .body(createErrorResponse(
                        status,
                        "Invalid request body or enum value",
                        request.getRequestURI()));
    }

    // Handle illegal argument errors (400)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        String message = ex.getMessage() != null
                ? ex.getMessage()
                : "Invalid request value";

        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status)
                .body(createErrorResponse(
                        status,
                        message,
                        request.getRequestURI()));
    }

    // Handle application-specific bad requests (400)
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status)
                .body(createErrorResponse(
                        status,
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    // Handle HTTP status errors, including duplicate categories
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {

        HttpStatusCode status = ex.getStatusCode();

        String message = ex.getReason() != null
                ? ex.getReason()
                : "Request failed";

        return ResponseEntity.status(status)
                .body(createErrorResponse(
                        status,
                        message,
                        request.getRequestURI()));
    }

    // Handle database constraint violations (409)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        logger.warn(
                "Database constraint violation on {} {}",
                request.getMethod(),
                request.getRequestURI());

        HttpStatus status = HttpStatus.CONFLICT;

        return ResponseEntity.status(status)
                .body(createErrorResponse(
                        status,
                        "The request conflicts with existing database data",
                        request.getRequestURI()));
    }

    // Handle transaction failures caused by persistence validation (400)
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Map<String, Object>> handleTransactionFailure(
            TransactionSystemException ex,
            HttpServletRequest request) {

        logger.warn(
                "Transaction failed on {} {}",
                request.getMethod(),
                request.getRequestURI(),
                ex);

        HttpStatus status = HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status)
                .body(createErrorResponse(
                        status,
                        "The request could not be saved. Check the submitted data.",
                        request.getRequestURI()));
    }

    // Handle failed authentication (401)
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationError(
            AuthenticationException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.UNAUTHORIZED;

        return ResponseEntity.status(status)
                .body(createErrorResponse(
                        status,
                        "Invalid username or password",
                        request.getRequestURI()));
    }

    // Log unexpected errors and return a safe response (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralError(
            Exception ex,
            HttpServletRequest request) {

        logger.error(
                "Unexpected error on {} {}",
                request.getMethod(),
                request.getRequestURI(),
                ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(status)
                .body(createErrorResponse(
                        status,
                        "An unexpected server error occurred",
                        request.getRequestURI()));
    }
}
