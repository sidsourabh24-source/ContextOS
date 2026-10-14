package com.contextos.retrieval.hybrid;

import com.contextos.ingestion.embedding.EmbeddingService;
import com.contextos.retrieval.keyword.KeywordSearchRepository;
import com.contextos.retrieval.keyword.KeywordSearchResult;
import com.contextos.retrieval.vector.VectorSearchRepository;
import com.contextos.retrieval.vector.VectorSearchResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
public class HybridSearchMerger {

    private final VectorSearchRepository vectorSearchRepository;
    private final KeywordSearchRepository keywordSearchRepository;
    private final EmbeddingService embeddingService;

    public List<CandidateChunk> searchAndMerge(UUID tenantId, String query, int topK) {
        if (tenantId == null || query == null || query.isBlank()) {
            return List.of();
        }

        // 1. Vector Search
        float[] queryVector = embeddingService.embed(query);
        List<VectorSearchResult> vectorResults = vectorSearchRepository.searchSimilarVectors(tenantId, queryVector, topK * 2);

        // 2. Keyword FTS Search
        List<KeywordSearchResult> keywordResults = keywordSearchRepository.searchByKeywords(tenantId, query, topK * 2);

        // 3. Deduplicate & Merge by Chunk ID
        Map<UUID, CandidateChunk> candidateMap = new HashMap<>();

        for (VectorSearchResult vRes : vectorResults) {
            candidateMap.put(vRes.chunkId(), new CandidateChunk(
                    vRes.chunkId(),
                    vRes.documentId(),
                    vRes.tenantId(),
                    vRes.content(),
                    vRes.cosineSimilarity(),
                    0.0,
                    vRes.chunkIndex(),
                    vRes.documentTitle(),
                    vRes.documentUrl(),
                    "DOCUMENT",
                    OffsetDateTime.now()
            ));
        }

        for (KeywordSearchResult kRes : keywordResults) {
            CandidateChunk existing = candidateMap.get(kRes.chunkId());
            double vectorSim = existing != null ? existing.vectorSimilarity() : 0.0;

            candidateMap.put(kRes.chunkId(), new CandidateChunk(
                    kRes.chunkId(),
                    kRes.documentId(),
                    kRes.tenantId(),
                    kRes.content(),
                    vectorSim,
                    kRes.keywordRank(),
                    kRes.chunkIndex(),
                    kRes.documentTitle(),
                    kRes.documentUrl(),
                    "DOCUMENT",
                    OffsetDateTime.now()
            ));
        }

        return new ArrayList<>(candidateMap.values());
    }
}
