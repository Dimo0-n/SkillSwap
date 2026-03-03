package com.example.skillswap.config;

import com.example.skillswap.exceptions.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "com.example.skillswap.controller")
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException exception, HttpServletRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", exception.getStatus().value());
        body.put("error", exception.getStatus().getReasonPhrase());
        body.put("message", exception.getMessage());
        body.put("path", request.getRequestURI());

        return ResponseEntity.status(exception.getStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpectedException(Exception exception, HttpServletRequest request) {
        log.error("Unexpected API error for path={}", request.getRequestURI(), exception);

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        body.put("message", "Internal server error");
        body.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
