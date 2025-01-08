package com.fletrax.tracking.document.service;

import com.fletrax.tracking.document.config.DocumentProperties;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class FileValidationService {

    private final DocumentProperties documentProperties;

    @Autowired
    public FileValidationService(DocumentProperties documentProperties) {
        this.documentProperties = documentProperties;
    }

    private boolean isValidFileSize(MultipartFile file) {
        return file.getSize() <= documentProperties.getMaxFileSize();
    }

    private boolean isValidExtension(String fileName) {
        if (fileName == null) return false;
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return documentProperties.getAllowedExtensions().contains(fileExtension);
    }

    private boolean isValidContentType(MultipartFile file) throws IOException {
        Tika tika = new Tika();
        String fileContentType = tika.detect(file.getInputStream());
        return documentProperties.getAllowedContentTypes().contains(fileContentType);
    }

    public boolean isValidFile(MultipartFile file) {
        if (!documentProperties.isFileValidationEnabled()) {
            return true;
        }
        try {
            return isValidFileSize(file) && isValidExtension(file.getOriginalFilename()) && isValidContentType(file);
        } catch (IOException e) {
            return false;
        }
    }
}