package com.contextos.ingestion;

import com.contextos.common.SourceItem;
import com.contextos.connector.ConnectorType;
import com.contextos.ingestion.model.Document;
import com.contextos.ingestion.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentNormalizerTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private DocumentNormalizer documentNormalizer;

    @Test
    void isModified_newDocument_returnsTrue() {
        UUID tenantId = UUID.randomUUID();
        SourceItem item = new SourceItem(
                "doc-1", ConnectorType.LOCAL, "url", "Title", "Content",
                "author", OffsetDateTime.now(), OffsetDateTime.now(), "text/plain",
                tenantId, "hash-123", Map.of()
        );

        when(documentRepository.findByTenantIdAndSourceId(tenantId, "doc-1")).thenReturn(Optional.empty());

        assertTrue(documentNormalizer.isModified(item));
    }

    @Test
    void isModified_sameChecksum_returnsFalse() {
        UUID tenantId = UUID.randomUUID();
        SourceItem item = new SourceItem(
                "doc-1", ConnectorType.LOCAL, "url", "Title", "Content",
                "author", OffsetDateTime.now(), OffsetDateTime.now(), "text/plain",
                tenantId, "same-checksum", Map.of()
        );

        Document existingDoc = Document.builder()
                .sourceId("doc-1")
                .checksum("same-checksum")
                .build();

        when(documentRepository.findByTenantIdAndSourceId(tenantId, "doc-1")).thenReturn(Optional.of(existingDoc));

        assertFalse(documentNormalizer.isModified(item));
    }
}
