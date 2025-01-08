package com.fletrax.tracking.document.controller;


import com.fletrax.tracking.document.repository.entity.DocumentMetadata;
import com.fletrax.tracking.document.service.DocumentService;
import com.fletrax.tracking.document.service.FileValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@RestController
@RequestMapping("/v1/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private FileValidationService fileValidationService;

    @PostMapping("/upload")
    public ResponseEntity<Object> uploadDocument(@RequestParam("file") MultipartFile file,
                                               @RequestParam Map<String, String> metadata,
                                               @RequestParam("relatedEntityId") String relatedEntityId,
                                               @RequestParam("relatedEntityType") String relatedEntityType) {
        if (!fileValidationService.isValidFile(file)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid file. Please ensure the file is of an acceptable size, type, and format.");
        }
        try {
            UUID documentId = documentService.saveDocument(file, metadata, relatedEntityId, relatedEntityType);
            return ResponseEntity.ok(documentId);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to store the file.");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> fetchDocument(@PathVariable UUID id) {
        try {
            DocumentMetadata metadata = documentService.getDocumentMetadata(id);
            byte[] documentData = documentService.fetchDocument(metadata);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(metadata.getContentType()));
            headers.setContentLength(metadata.getSize());
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=document.pdf");

            return new ResponseEntity<>(documentData, headers, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
