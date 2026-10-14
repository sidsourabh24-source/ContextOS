package com.contextos.common;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record ContextPackage(
    String requestId,
    String query,
    OffsetDateTime generatedAt,
    List<CitationSource> sources,
    String formattedContext,
    int estimatedTokenCount,
    RetrievalMetrics retrievalMetrics
) {
    public record CitationSource(
        UUID chunkId,
        UUID documentId,
        String title,
        String url,
        String sourceType,
        String excerpt,
        double relevanceScore,
        OffsetDateTime updatedAt
    ) {}

    public record RetrievalMetrics(
        int keywordCandidates,
        int vectorCandidates,
        int rerankedCandidates,
        long latencyMs
    ) {}
}
