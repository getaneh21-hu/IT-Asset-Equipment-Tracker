
package com.It_Asset.it_asset_equipment_tracker.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;

import org.springframework.security.core.AuthenticationException;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private Map<String, Object> createErrorResponse(
            HttpStatus status,
            String message,
            String path) {

        Map<String, Object> error = new LinkedHashMap<>();

        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
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

    // Handle invalid JSON or request body
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

        return ResponseEntity.badRequest()
                .body(createErrorResponse(
                        HttpStatus.BAD_REQUEST,
                        ex.getMessage(),
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

    // Handle failed authentication: incorrect username or password
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
                ex
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected server error occurred",
                        request.getRequestURI()));
    }
}
