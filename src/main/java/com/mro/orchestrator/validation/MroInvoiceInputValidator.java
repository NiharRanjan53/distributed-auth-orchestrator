package com.mro.orchestrator.validation;

import com.mro.orchestrator.exception.FileValidationException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Component
public class MroInvoiceInputValidator implements FileValidator {

    // Logic Preserved: Required patterns for MRO ingestion
    private static final Map<String, List<String>> REQUIRED_PATTERNS = Map.of(
            "data", List.of(".xlsx", ".xls"),
            "config", List.of(".xlsx", ".xls"),
            "document", List.of(".pdf")
    );

    @Override
    public void validate(List<MultipartFile> files) {
        Set<String> foundPatterns = new HashSet<>();
        List<String> invalidFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            String filename = Optional.ofNullable(file.getOriginalFilename())
                    .map(String::toLowerCase)
                    .orElse("");

            if (!filename.contains(".")) {
                invalidFiles.add(file.getOriginalFilename());
                continue;
            }

            String extension = filename.substring(filename.lastIndexOf("."));

            // Logic Preserved: Check if extension is allowed at all
            boolean isAllowedExt = REQUIRED_PATTERNS.values().stream()
                    .anyMatch(exts -> exts.contains(extension));

            if (!isAllowedExt) {
                invalidFiles.add(file.getOriginalFilename());
                continue;
            }

            // Logic Preserved: Match against required MRO patterns
            REQUIRED_PATTERNS.forEach((pattern, exts) -> {
                if (filename.contains(pattern) && exts.contains(extension)) {
                    foundPatterns.add(pattern);
                }
            });
        }

        // Logic Preserved: Throw exception for invalid extensions
        if (!invalidFiles.isEmpty()) {
            throw new FileValidationException("Invalid file types: " + invalidFiles);
        }

        // Logic Preserved: Check missing required components
        Set<String> missing = new HashSet<>(REQUIRED_PATTERNS.keySet());
        missing.removeAll(foundPatterns);

        if (!missing.isEmpty()) {
            throw new FileValidationException("Missing required MRO files: " + missing);
        }
    }
}