package com.fletrax.tracking.document.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "document")
@Getter
@Setter
public class DocumentProperties {
    private String storagePath;
    private boolean fileValidationEnabled;
    private long maxFileSize;
    private List<String> allowedExtensions;
    private List<String> allowedContentTypes;
}