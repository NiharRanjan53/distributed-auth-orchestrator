package com.mro.orchestrator.controller;

import com.mro.orchestrator.dto.FileUploadResponseDTO;
import com.mro.orchestrator.service.DocumentUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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
            @AuthenticationPrincipal UserDetails userDetails) {

        // Logic: In a real app, you'd fetch the numeric ID from your custom UserDetails.
        // For now, we use a placeholder or extract it from the userDetails service.
        Long userId = 1L; // Replace with actual userId extraction logic

        FileUploadResponseDTO response = documentUploadService.handleUpload(files, userId);

        return ResponseEntity.ok(response);
    }
}