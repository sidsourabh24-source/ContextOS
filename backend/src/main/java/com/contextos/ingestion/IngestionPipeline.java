package com.contextos.ingestion;

import com.contextos.common.SourceItem;
import com.contextos.connector.model.Connector;
import com.contextos.ingestion.chunker.StructureChunker;
import com.contextos.ingestion.embedding.EmbeddingService;
import com.contextos.ingestion.model.Chunk;
import com.contextos.ingestion.model.Document;
import com.contextos.ingestion.repository.ChunkRepository;
import com.contextos.ingestion.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IngestionPipeline {

    private final DocumentNormalizer documentNormalizer;
    private final StructureChunker structureChunker;
    private final EmbeddingService embeddingService;
    private final DocumentRepository documentRepository;
    private final ChunkRepository chunkRepository;

    @Transactional
    public List<Document> processSourceItems(Connector connector, List<SourceItem> items) {
        List<Document> processedDocs = new ArrayList<>();

        for (SourceItem item : items) {
            if (!documentNormalizer.isModified(item)) {
                // Skip unchanged document
                documentRepository.findByTenantIdAndSourceId(item.tenantId(), item.sourceId())
                        .ifPresent(processedDocs::add);
                continue;
            }

            // Save or Update Document
            Optional<Document> existingDocOpt = documentRepository.findByTenantIdAndSourceId(
                    item.tenantId(), item.sourceId()
            );

            Document document;
            if (existingDocOpt.isPresent()) {
                document = existingDocOpt.get();
                document.setTitle(item.title());
                document.setUrl(item.sourceUrl());
                document.setChecksum(item.checksum());
                document.setUpdatedAt(OffsetDateTime.now());

                // Delete old chunks before re-chunking
                List<Chunk> oldChunks = chunkRepository.findByDocumentIdOrderByChunkIndexAsc(document.getId());
                chunkRepository.deleteAll(oldChunks);
            } else {
                document = Document.builder()
                        .tenant(connector.getTenant())
                        .connector(connector)
                        .sourceId(item.sourceId())
                        .title(item.title())
                        .url(item.sourceUrl())
                        .checksum(item.checksum())
                        .updatedAt(OffsetDateTime.now())
                        .build();
            }

            document = documentRepository.save(document);

            // Chunk & Embed Content
            List<StructureChunker.ChunkResult> chunkResults = structureChunker.chunkDocument(item.content());
            List<Chunk> chunkEntities = new ArrayList<>();

            for (StructureChunker.ChunkResult res : chunkResults) {
                float[] embedding = embeddingService.embed(res.content());

                Chunk chunk = Chunk.builder()
                        .document(document)
                        .tenant(connector.getTenant())
                        .chunkIndex(res.index())
                        .content(res.content())
                        .tokenCount(res.tokenCount())
                        .build();
                chunkEntities.add(chunk);
            }

            chunkRepository.saveAll(chunkEntities);
            processedDocs.add(document);
        }

        return processedDocs;
    }
}
