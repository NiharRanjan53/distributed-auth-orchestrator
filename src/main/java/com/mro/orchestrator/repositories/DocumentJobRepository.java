package com.mro.orchestrator.repositories;

import com.mro.orchestrator.models.DocumentJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentJobRepository extends JpaRepository<DocumentJob, String> {
    // You can find a job by its UUID
    Optional<DocumentJob> findByJobId(String jobId);

    // Custom query to find all jobs for a specific user (useful for the UI)
    List<DocumentJob> findByUserIdOrderByCreatedAtDesc(Long userId);
}