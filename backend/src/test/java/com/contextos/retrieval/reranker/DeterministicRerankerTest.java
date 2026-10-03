package com.contextos.retrieval.reranker;

import com.contextos.retrieval.hybrid.CandidateChunk;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DeterministicRerankerTest {

    private final DeterministicReranker reranker = new DeterministicReranker();

    @Test
    void rerank_multipleCandidates_sortsByHighestWeightedScore() {
        UUID tenantId = UUID.randomUUID();

        CandidateChunk candidateLow = new CandidateChunk(
                UUID.randomUUID(), UUID.randomUUID(), tenantId, "Low Relevance",
                0.2, 0.1, 0, "Doc A", "urlA", "LOCAL", OffsetDateTime.now().minusDays(100)
        );

        CandidateChunk candidateHigh = new CandidateChunk(
                UUID.randomUUID(), UUID.randomUUID(), tenantId, "High Relevance",
                0.9, 0.8, 0, "Doc B", "urlB", "GITHUB", OffsetDateTime.now()
        );

        List<RankedChunk> ranked = reranker.rerank(List.of(candidateLow, candidateHigh));

        assertEquals(2, ranked.size());
        assertEquals("High Relevance", ranked.get(0).chunk().content());
        assertTrue(ranked.get(0).finalScore() > ranked.get(1).finalScore());
    }
}
