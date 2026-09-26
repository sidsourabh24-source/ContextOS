package com.contextos.common;

import com.contextos.connector.ConnectorType;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public record SourceItem(
    String sourceId,
    ConnectorType sourceType,
    String sourceUrl,
    String title,
    String content,
    String author,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt,
    String mimeType,
    UUID tenantId,
    String checksum,
    Map<String, Object> metadata
) {
    public SourceItem {
        if (sourceId == null || sourceId.isBlank()) {
            throw new IllegalArgumentException("sourceId cannot be null or blank");
        }
        if (sourceType == null) {
            throw new IllegalArgumentException("sourceType cannot be null");
        }
        if (tenantId == null) {
            throw new IllegalArgumentException("tenantId cannot be null");
        }
        if (checksum == null || checksum.isBlank()) {
            throw new IllegalArgumentException("checksum cannot be null or blank");
        }
        metadata = metadata != null ? Map.copyOf(metadata) : Map.of();
    }
}
