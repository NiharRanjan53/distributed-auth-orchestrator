package com.mro.orchestrator.service;

import com.mro.orchestrator.dto.FileUploadResponseDTO;
import com.mro.orchestrator.models.DocumentJob;
import com.mro.orchestrator.models.DocumentMetadata;
import com.mro.orchestrator.repositories.DocumentJobRepository;
import com.mro.orchestrator.validation.ValidationEngine;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentUploadService {

    private final S3Service s3Service;
    private final DocumentJobRepository documentRepository;
    private final ValidationEngine validationEngine;
    // private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public FileUploadResponseDTO handleUpload(List<MultipartFile> files, Long userId) {
        DocumentJob job = Objects.requireNonNull(
                documentRepository.save(
                        DocumentJob.builder()
                                .userId(userId)
                                .status("INGESTION_STARTED")
                                .build()),
                "Failed to create ingestion job");

        String jobId = job.getJobId();
        log.info("Starting ingestion job: {} for user: {}", jobId, userId);
        System.out.println("==========================");

        // 1. Logic Preserved: Run the Validation Engine (Decoupled logic)
        validationEngine.runAll(files);

        try {
            // 2. Optimized & Safe S3 Upload
            // Logic Change: We use .map() instead of .forEach() to ensure thread-safety
            // when collecting S3 paths in a parallelStream.
            List<String> s3Paths = files.parallelStream()
                    .map(file -> {
                        String s3Key = String.format("mro/%s/%s", jobId, file.getOriginalFilename());
                        return s3Service.uploadFile(file, s3Key);
                    })
                    .collect(Collectors.toList());

            // 3. Database Persistence Logic (New Industry Standard)
            // We update the existing parent Job and attach metadata records.
            job.setStatus("RAW_INGESTED");

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                DocumentMetadata metadata = DocumentMetadata.builder()
                        .fileName(file.getOriginalFilename())
                        .s3Url(s3Paths.get(i))
                        .fileType(file.getContentType())
                        .documentJob(job) // Link child to parent
                        .build();
                job.getFiles().add(metadata); // Link parent to child
            }

            documentRepository.save(job);

            // 4. Logic Preserved: Kafka Event Preparation (publish step still disabled)
            // kafkaTemplate.send("raw-document-ingested", jobId, eventPayload);
            log.info("Ingestion event prepared for job: {} with {} files", jobId, files.size());

            // 5. Updated Return Type (DTO instead of Map)
            return new FileUploadResponseDTO(jobId, s3Paths, "Upload successful");

        } catch (Exception e) {
            log.error("Critical failure in job {}: {}", jobId, e.getMessage());
            throw new RuntimeException("Upload failed due to internal service error");
        }
    }
}