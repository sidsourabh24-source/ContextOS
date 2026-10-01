package com.contextos.retrieval.vector;

import java.util.UUID;

public record VectorSearchResult(
    UUID chunkId,
    UUID documentId,
    UUID tenantId,
    String content,
    double cosineSimilarity,
    int chunkIndex,
    String documentTitle,
    String documentUrl
) {}
