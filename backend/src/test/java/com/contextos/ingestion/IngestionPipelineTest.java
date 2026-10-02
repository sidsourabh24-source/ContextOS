package com.contextos.ingestion;

import com.contextos.common.SourceItem;
import com.contextos.connector.ConnectorType;
import com.contextos.connector.model.Connector;
import com.contextos.ingestion.chunker.StructureChunker;
import com.contextos.ingestion.embedding.EmbeddingService;
import com.contextos.ingestion.model.Document;
import com.contextos.ingestion.repository.ChunkRepository;
import com.contextos.ingestion.repository.DocumentRepository;
import com.contextos.tenant.model.Tenant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IngestionPipelineTest {

    @Mock
    private DocumentNormalizer documentNormalizer;

    @Mock
    private StructureChunker structureChunker;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ChunkRepository chunkRepository;

    @InjectMocks
    private IngestionPipeline ingestionPipeline;

    @Test
    void processSourceItems_newItem_chunksEmbedsAndSaves() {
        UUID tenantId = UUID.randomUUID();
        Tenant tenant = Tenant.builder().id(tenantId).name("Test Corp").build();
        Connector connector = Connector.builder().id(UUID.randomUUID()).tenant(tenant).type("LOCAL").build();

        SourceItem item = new SourceItem(
                "doc-1", ConnectorType.LOCAL, "url", "Title", "# Hello World",
                "author", OffsetDateTime.now(), OffsetDateTime.now(), "text/markdown",
                tenantId, "hash-1", Map.of()
        );

        Document savedDoc = Document.builder().id(UUID.randomUUID()).sourceId("doc-1").tenant(tenant).build();

        when(documentNormalizer.isModified(item)).thenReturn(true);
        when(documentRepository.findByTenantIdAndSourceId(tenantId, "doc-1")).thenReturn(Optional.empty());
        when(documentRepository.save(any())).thenReturn(savedDoc);
        when(structureChunker.chunkDocument(anyString())).thenReturn(List.of(new StructureChunker.ChunkResult(0, "# Hello World", 3)));
        when(embeddingService.embed(anyString())).thenReturn(new float[1536]);

        List<Document> result = ingestionPipeline.processSourceItems(connector, List.of(item));

        assertEquals(1, result.size());
        verify(documentRepository).save(any());
        verify(chunkRepository).saveAll(any());
    }
}
