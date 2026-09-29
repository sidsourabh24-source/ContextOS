package com.contextos.ingestion;

import com.contextos.common.SourceItem;
import com.contextos.ingestion.model.Document;
import com.contextos.ingestion.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DocumentNormalizer {

    private final DocumentRepository documentRepository;

    public boolean isModified(SourceItem item) {
        Optional<Document> existingDocOpt = documentRepository.findByTenantIdAndSourceId(
                item.tenantId(), item.sourceId()
        );

        if (existingDocOpt.isEmpty()) {
            return true; // New document
        }

        Document existingDoc = existingDocOpt.get();
        return !existingDoc.getChecksum().equals(item.checksum());
    }
}
