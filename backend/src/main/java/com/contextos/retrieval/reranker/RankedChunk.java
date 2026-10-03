package com.contextos.retrieval.reranker;

import com.contextos.retrieval.hybrid.CandidateChunk;

public record RankedChunk(
    CandidateChunk chunk,
    double finalScore,
    double semanticScore,
    double keywordScore,
    double freshnessScore,
    double sourcePriorityScore
) {}
