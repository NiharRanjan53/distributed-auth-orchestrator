package com.mro.orchestrator.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "document_jobs", uniqueConstraints = {
        @UniqueConstraint(name = "uk_document_jobs_user_idempotency", columnNames = { "user_id", "idempotency_key" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "job_id", nullable = false, updatable = false)
    private String jobId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(nullable = false)
    private String status;

    @OneToMany(mappedBy = "documentJob", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DocumentMetadata> files = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}