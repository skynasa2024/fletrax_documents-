package com.fletrax.tracking.document.service;


import com.fletrax.tracking.document.config.DocumentProperties;
import com.fletrax.tracking.document.repository.DocumentRepository;
import com.fletrax.tracking.document.repository.entity.DocumentMetadata;
import com.fletrax.tracking.document.repository.entity.FileStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;


@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    private final Path storagePath;

    @Autowired
    public DocumentService(DocumentProperties documentProperties) {
        this.storagePath = Paths.get(documentProperties.getStoragePath());
    }

    public UUID saveDocument(MultipartFile file,
                             Map<String, String> metadata,
                             String relatedEntityId,
                             String relatedEntityType) throws Exception {
        if (file.isEmpty()) {
            throw new Exception("File is empty");
        }

        SecretKey secretKey = KeyGenerator.getInstance("AES").generateKey();
        String keyString = Base64.getEncoder().encodeToString(secretKey.getEncoded());

        DocumentMetadata documentMetadata = DocumentMetadata.builder()
                .name(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .encryptionKey(keyString)
                .createdAt(OffsetDateTime.now())
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .status(FileStatus.IN_PROGRESS)
                .build();
        DocumentMetadata savedDocument = documentRepository.save(documentMetadata);

        Path documentPath = storagePath.resolve(savedDocument.getId().toString());
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        try (FileOutputStream fos = new FileOutputStream(documentPath.toFile());
             CipherOutputStream cos = new CipherOutputStream(fos, cipher)) {
            cos.write(file.getBytes());
            cos.flush();
            savedDocument.setStatus(FileStatus.STORED);
        } catch (Exception e) {
            savedDocument.setStatus(FileStatus.FAILED_TO_STORE);
            e.printStackTrace();
        }
        documentRepository.save(savedDocument);
        return savedDocument.getId();
    }

    public DocumentMetadata getDocumentMetadata(UUID documentId) {
        return documentRepository.findById(documentId).orElse(null);
    }

    public byte[] fetchDocument (DocumentMetadata metadata) throws FileNotFoundException {
        Path documentPath = storagePath.resolve(String.valueOf(metadata.getId()));
        if (!Files.exists(documentPath)) {
            throw new FileNotFoundException("Document not found");
        }

        try {
            Cipher cipher = Cipher.getInstance("AES");
            SecretKey secretKey = new SecretKeySpec(Base64.getDecoder().decode(metadata.getEncryptionKey()), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] encryptedData = Files.readAllBytes(documentPath);
            return cipher.doFinal(encryptedData);
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("");
        }
    }
}
