package com.mro.orchestrator.repositories;

import com.mro.orchestrator.models.DocumentJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentJobRepository extends JpaRepository<DocumentJob, String> {
    Optional<DocumentJob> findByUserIdAndIdempotencyKey(Long userId, String idempotencyKey);

    List<DocumentJob> findByUserIdOrderByCreatedAtDesc(Long userId);
}