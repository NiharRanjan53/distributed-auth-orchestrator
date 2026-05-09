package com.mro.orchestrator.service.impl;

import com.mro.orchestrator.config.KafkaConfig;
import com.mro.orchestrator.dto.FileUploadResponseDTO;
import com.mro.orchestrator.events.RawDocumentIngestedEvent;
import com.mro.orchestrator.models.DocumentJob;
import com.mro.orchestrator.models.DocumentMetadata;
import com.mro.orchestrator.producers.KafkaEventProducer;
import com.mro.orchestrator.repositories.DocumentJobRepository;
import com.mro.orchestrator.service.IDocumentUploadService;
import com.mro.orchestrator.service.S3Service;
import com.mro.orchestrator.validation.ValidationEngine;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentUploadService implements IDocumentUploadService {

    private final S3Service s3Service;
    private final DocumentJobRepository documentRepository;
    private final ValidationEngine validationEngine;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaEventProducer kafkaEventProducer;

    @Transactional
    public FileUploadResponseDTO handleUpload(List<MultipartFile> files, Long userId) {
        // 1. CRITICAL: Validate BEFORE saving anything to the DB or S3
        validationEngine.runAll(files);

        // 2. Create the initial Job record
        DocumentJob job = documentRepository.save(
                DocumentJob.builder()
                        .userId(userId)
                        .status("INGESTION_STARTED")
                        .files(new ArrayList<>()) // Ensure list is initialized
                        .build());

        String jobId = job.getJobId();
        log.info("Starting ingestion job: {} for user: {}", jobId, userId);

        try {
            // 3. Parallel S3 Upload (Thread-safe mapping)
            List<String> s3Paths = files.parallelStream()
                    .map(file -> {
                        String s3Key = String.format("mro/%s/%s", jobId, file.getOriginalFilename());
                        return s3Service.uploadFile(file, s3Key);
                    })
                    .collect(Collectors.toList());

            // 4. Update Job and Attach Metadata
            job.setStatus("RAW_INGESTED");

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                DocumentMetadata metadata = DocumentMetadata.builder()
                        .fileName(file.getOriginalFilename())
                        .s3Url(s3Paths.get(i))
                        .fileType(file.getContentType())
                        .documentJob(job)
                        .build();
                job.getFiles().add(metadata);
            }

            documentRepository.save(job);

            // 5. Multi-Topic Kafka Integration
            RawDocumentIngestedEvent ingestionEvent = RawDocumentIngestedEvent.builder()
                    .jobId(jobId)
                    .userId(userId)
                    .filePaths(s3Paths)
                    .fileCount(files.size())
                    .status("UPLOADED")
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                    .build();

            // Send to Ingestion Topic
            kafkaEventProducer.publishrawDocumentIngestedEvent(ingestionEvent);

            // Send to Audit Topic (Optional but recommended)
            kafkaTemplate.send(KafkaConfig.AUDIT_TOPIC, jobId, Map.of(
                    "action", "UPLOAD_SUCCESS",
                    "userId", userId,
                    "jobId", jobId
            ));

            log.info("Ingestion events produced for job: {}", jobId);

            // 6. Return Structured DTO
            return new FileUploadResponseDTO(jobId, s3Paths, "MRO documents uploaded successfully.");

        } catch (Exception e) {
            log.error("Critical failure in job {}: {}", jobId, e.getMessage());
            // Transactional will roll back the Database, but S3 files remain.
            // will trigger an S3 cleanup here.
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }
}