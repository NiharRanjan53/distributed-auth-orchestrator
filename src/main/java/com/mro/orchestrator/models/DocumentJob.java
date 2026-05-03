package com.mro.orchestrator.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "document_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentJob {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String jobId;

    private Long userId;

    private String status;

    // 'mappedBy' tells Hibernate: "Look at the 'documentJob' field inside DocumentMetadata"
    @OneToMany(mappedBy = "documentJob", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DocumentMetadata> files = new ArrayList<>();

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}