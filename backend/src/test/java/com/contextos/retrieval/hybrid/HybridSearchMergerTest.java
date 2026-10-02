package com.contextos.retrieval.hybrid;

import com.contextos.ingestion.embedding.EmbeddingService;
import com.contextos.retrieval.keyword.KeywordSearchRepository;
import com.contextos.retrieval.keyword.KeywordSearchResult;
import com.contextos.retrieval.vector.VectorSearchRepository;
import com.contextos.retrieval.vector.VectorSearchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HybridSearchMergerTest {

    @Mock
    private VectorSearchRepository vectorSearchRepository;

    @Mock
    private KeywordSearchRepository keywordSearchRepository;

    @Mock
    private EmbeddingService embeddingService;

    @InjectMocks
    private HybridSearchMerger hybridSearchMerger;

    @Test
    void searchAndMerge_combinesVectorAndKeywordResults_deduplicatesByChunkId() {
        UUID tenantId = UUID.randomUUID();
        UUID chunkId = UUID.randomUUID();
        UUID docId = UUID.randomUUID();

        VectorSearchResult vRes = new VectorSearchResult(chunkId, docId, tenantId, "Content", 0.85, 0, "Title", "url");
        KeywordSearchResult kRes = new KeywordSearchResult(chunkId, docId, tenantId, "Content", 0.45, 0, "Title", "url");

        when(embeddingService.embed(anyString())).thenReturn(new float[1536]);
        when(vectorSearchRepository.searchSimilarVectors(eq(tenantId), any(), anyInt())).thenReturn(List.of(vRes));
        when(keywordSearchRepository.searchByKeywords(eq(tenantId), anyString(), anyInt())).thenReturn(List.of(kRes));

        List<CandidateChunk> candidates = hybridSearchMerger.searchAndMerge(tenantId, "APOLLO fix", 5);

        assertEquals(1, candidates.size());
        assertEquals(chunkId, candidates.get(0).chunkId());
        assertEquals(0.85, candidates.get(0).vectorSimilarity());
        assertEquals(0.45, candidates.get(0).keywordRank());
    }
}
