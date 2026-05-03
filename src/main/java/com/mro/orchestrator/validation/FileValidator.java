package com.mro.orchestrator.validation;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface FileValidator {
    void validate(List<MultipartFile> files);
}