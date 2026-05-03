package com.mro.orchestrator.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ValidationEngine {

    // Spring automatically injects MroPatternValidator into this list
    private final List<FileValidator> validators;

    public void runAll(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("No files provided for validation");
        }
        validators.forEach(v -> v.validate(files));
    }
}