package com.contextos.retrieval.hybrid;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CandidateChunk(
    UUID chunkId,
    UUID documentId,
    UUID tenantId,
    String content,
    double vectorSimilarity,
    double keywordRank,
    int chunkIndex,
    String documentTitle,
    String documentUrl,
    String sourceType,
    OffsetDateTime updatedAt
) {}
