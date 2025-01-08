package com.fletrax.tracking.document.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Builder
@Table(name = "document")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DocumentMetadata {
    @Id
    @UuidGenerator
    private UUID id;
    private String name;
    @Column(length = 80)
    private String contentType;
    private long size;
    @Column(length = 100)
    private String encryptionKey;
    @CreationTimestamp
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    private OffsetDateTime lastUpdatedDate;
    private String relatedEntityId;
    private String relatedEntityType;
    @Column(length = 80)
    @Enumerated(EnumType.STRING)
    private FileStatus status;
}