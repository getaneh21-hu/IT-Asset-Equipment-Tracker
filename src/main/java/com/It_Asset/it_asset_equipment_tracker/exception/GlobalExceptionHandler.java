
package com.It_Asset.it_asset_equipment_tracker.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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
            HttpStatus status,
            String message,
            String path) {

        Map<String, Object> error = new LinkedHashMap<>();

        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", status.getReasonPhrase());
        error.put("message", message);
        error.put("path", path);

        return error;
    }

    // Handle resources that cannot be found
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse(
                        HttpStatus.NOT_FOUND,
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    // Handle validation errors
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

        return ResponseEntity.badRequest()
                .body(createErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        message,
                        request.getRequestURI()));
    }

    // Handle invalid JSON, request bodies, or enum values
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRequest(
            HttpMessageNotReadableException ex,
            HttpServletRequest request) {

        return ResponseEntity.badRequest()
                .body(createErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        "Invalid request body or enum value",
                        request.getRequestURI()));
    }

    // Handle illegal argument errors
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request) {

        String message = ex.getMessage() != null
                ? ex.getMessage()
                : "Invalid request value";

        return ResponseEntity.badRequest()
                .body(createErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        message,
                        request.getRequestURI()));
    }

    // Handle application-specific bad requests
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(
            BadRequestException ex,
            HttpServletRequest request) {

        return ResponseEntity.badRequest()
                .body(createErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
                        request.getRequestURI()));
    }

    // Handle HTTP status errors, including duplicate categories
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request) {

        HttpStatus status = HttpStatus.valueOf(
                ex.getStatusCode().value());

        String message = ex.getReason() != null
                ? ex.getReason()
                : status.getReasonPhrase();

        return ResponseEntity.status(status)
                .body(createErrorResponse(
                        status,
                        message,
                        request.getRequestURI()));
    }

    // Handle database constraint violations
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        logger.warn(
                "Database constraint violation on {} {}",
                request.getMethod(),
                request.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse(
                        HttpStatus.CONFLICT,
                        "The request conflicts with existing database data",
                        request.getRequestURI()));
    }

    // Handle transaction failures caused by persistence validation
    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Map<String, Object>> handleTransactionFailure(
            TransactionSystemException ex,
            HttpServletRequest request) {

        logger.warn(
                "Transaction failed on {} {}",
                request.getMethod(),
                request.getRequestURI(),
                ex);

        return ResponseEntity.badRequest()
                .body(createErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        "The request could not be saved. Check the submitted data.",
                        request.getRequestURI()));
    }

    // Handle failed authentication
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationError(
            AuthenticationException ex,
            HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(createErrorResponse(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid username or password",
                        request.getRequestURI()));
    }

    // Log unexpected errors and return a safe response
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralError(
            Exception ex,
            HttpServletRequest request) {

        logger.error(
                "Unexpected error on {} {}",
                request.getMethod(),
                request.getRequestURI(),
                ex);

        return ResponseEntity.status(
                        HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected server error occurred",
                        request.getRequestURI()));
    }
}
