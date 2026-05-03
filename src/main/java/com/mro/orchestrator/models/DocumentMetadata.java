package com.mro.orchestrator.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "document_metadata")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String fileName;
    private String s3Url;
    private String fileType;

    // This is the "Inverse Attribute" the parent is looking for
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id") // The foreign key column in this table
    private DocumentJob documentJob;
}