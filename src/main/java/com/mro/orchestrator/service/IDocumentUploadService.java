package com.mro.orchestrator.service;

import com.mro.orchestrator.dto.FileUploadResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IDocumentUploadService {
    FileUploadResponseDTO handleUpload(List<MultipartFile> files, Long userId, String jobId);
}
