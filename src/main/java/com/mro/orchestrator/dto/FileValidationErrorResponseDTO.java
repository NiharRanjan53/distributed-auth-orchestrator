package com.mro.orchestrator.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileValidationErrorResponseDTO {

    private int status; // e.g., 400
    private String error; // e.g., "Bad Request"
    private String message; // e.g., "File validation failed"
    private LocalDateTime timestamp;

}