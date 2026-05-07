package com.mro.orchestrator.controller;

import com.mro.orchestrator.dto.FileUploadResponseDTO;
import com.mro.orchestrator.service.DocumentUploadService;
import com.mro.orchestrator.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentUploadService documentUploadService;

    /**
     * Endpoint to handle multi-file MRO document ingestion.
     * Requires: Bearer Token with ROLE_ADMIN or ROLE_MECHANIC.
     */
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'BILLER')")
    public ResponseEntity<FileUploadResponseDTO> uploadMroDocuments(
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal AuthenticatedUser principal) {

        Long userId = principal.getId();

        FileUploadResponseDTO response = documentUploadService.handleUpload(files, userId);
        return ResponseEntity.ok(response);
    }
}