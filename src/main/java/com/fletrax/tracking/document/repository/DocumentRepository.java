package com.fletrax.tracking.document.repository;


import com.fletrax.tracking.document.repository.entity.DocumentMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<DocumentMetadata, UUID> {

    @Override
    Optional<DocumentMetadata> findById(UUID uuid);
}
