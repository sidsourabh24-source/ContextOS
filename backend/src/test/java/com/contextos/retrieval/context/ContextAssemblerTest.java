package com.contextos.retrieval.context;

import com.contextos.common.ContextPackage;
import com.contextos.ingestion.chunker.StructureChunker;
import com.contextos.retrieval.hybrid.CandidateChunk;
import com.contextos.retrieval.reranker.RankedChunk;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ContextAssemblerTest {

    private final StructureChunker chunker = new StructureChunker();
    private final ContextAssembler assembler = new ContextAssembler(chunker);

    @Test
    void assembleContext_respectsTokenBudget_buildsXMLBoundaries() {
        CandidateChunk candidate = new CandidateChunk(
                UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                "Sample context content paragraph", 0.9, 0.8, 0, "Title", "url", "GITHUB", OffsetDateTime.now()
        );
        RankedChunk ranked = new RankedChunk(candidate, 0.95, 0.9, 0.8, 1.0, 0.9);

        ContextPackage pkg = assembler.assembleContext("Why did Apollo fail?", List.of(ranked), 1000, 45L);

        assertNotNull(pkg);
        assertTrue(pkg.formattedContext().contains("<retrieved_document"));
        assertEquals(1, pkg.sources().size());
        assertEquals("Title", pkg.sources().get(0).title());
    }
}
