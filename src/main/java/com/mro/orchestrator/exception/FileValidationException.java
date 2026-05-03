package com.mro.orchestrator.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception thrown when a file fails MRO validation checks
 * (e.g., incorrect format, missing required patterns, or size limit exceeded).
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FileValidationException extends RuntimeException {

    public FileValidationException(String message) {
        super(message);
    }

    public FileValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}