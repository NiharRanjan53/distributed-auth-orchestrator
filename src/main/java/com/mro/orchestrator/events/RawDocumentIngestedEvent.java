package com.mro.orchestrator.events;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawDocumentIngestedEvent {
    private String jobId;      // Primary key for tracking
    private Long userId;       // The user who uploaded
    private List<String> filePaths; // List of S3 keys
    private int fileCount;     // Verification count
    private String timestamp;  // ISO-8601 formatted time
    private String status;     // e.g., "RAW_INGESTED"
}