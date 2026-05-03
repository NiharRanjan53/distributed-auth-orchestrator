package com.mro.orchestrator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class FileUploadResponseDTO {
    private String jobId;
    private List<String> filePaths;
    private String message;
}