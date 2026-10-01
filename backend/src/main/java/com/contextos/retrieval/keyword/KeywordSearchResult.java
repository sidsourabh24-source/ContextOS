package com.contextos.retrieval.keyword;

import java.util.UUID;

public record KeywordSearchResult(
    UUID chunkId,
    UUID documentId,
    UUID tenantId,
    String content,
    double keywordRank,
    int chunkIndex,
    String documentTitle,
    String documentUrl
) {}
