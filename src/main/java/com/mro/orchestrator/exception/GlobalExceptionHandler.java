package com.mro.orchestrator.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserExists(UserAlreadyExistsException ex) {
        // This returns {"error": "Username is already taken!"} instead of the full DTO with nulls
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }
}