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
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentUploadService implements IDocumentUploadService {
    private final RedissonClient redissonClient; // Injected Redisson Client
    private final S3Service s3Service;
    private final DocumentJobRepository documentRepository;
    private final ValidationEngine validationEngine;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaEventProducer kafkaEventProducer;

    @Transactional
    public FileUploadResponseDTO handleUpload(List<MultipartFile> files, Long userId, String idempotencyKey) {

        // 1. Acquire Distributed Lock using Redis
        String lockKey = "lock:upload:" + userId + ":" + idempotencyKey;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // Attempt to acquire lock for 5 seconds, release after 10 seconds automatically
            if (lock.tryLock(10, TimeUnit.SECONDS)) {
                log.info("Lock acquired for key: {}. Go check Redis Insight now! Sleeping for 20s...", lockKey);
                Thread.sleep(20000);
                // 2. Idempotency Check: Does this Job ID already exist in DB?
                Optional<DocumentJob> existingJob = documentRepository.findByUserIdAndIdempotencyKey(userId,
                        idempotencyKey);
                if (existingJob.isPresent()) {
                    return buildExistingResponse(existingJob.get());
                }

                // 3. Normal Flow: Validation -> DB Save -> S3 -> Kafka
                validationEngine.runAll(files);

                DocumentJob job = documentRepository.save(
                        DocumentJob.builder()
                                .userId(userId)
                                .idempotencyKey(idempotencyKey)
                                .status("INGESTION_STARTED")
                                .build());

                String jobId = job.getJobId();

                // Parallel S3 Upload
                List<String> s3Paths = files.parallelStream()
                        .map(file -> {
                            String s3Key = String.format("mro/%s/%s", idempotencyKey, file.getOriginalFilename());
                            return s3Service.uploadFile(file, s3Key);
                        })
                        .collect(Collectors.toList());

                log.info("Starting ingestion job: {} for user: {}", jobId, userId);

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
                        "jobId", jobId));

                log.info("Ingestion events produced for job: {}", jobId);

                // 6. Return Structured DTO
                return new FileUploadResponseDTO(jobId, s3Paths, "MRO documents uploaded successfully.");
            } else {
                // If the lock couldn't be acquired, another server is already working on this
                // jobId
                throw new RuntimeException("Upload already in progress for this Job ID.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while acquiring lock.");
        } finally {
            // 4. Always release the lock if held by this thread
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private FileUploadResponseDTO buildExistingResponse(DocumentJob job) {
        List<String> s3Paths = job.getFiles().stream()
                .map(DocumentMetadata::getS3Url)
                .toList();
        return new FileUploadResponseDTO(
                job.getJobId(),
                s3Paths,
                "Duplicate request detected. Returning existing upload result.");
    }
}
