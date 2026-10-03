package com.contextos.retrieval.reranker;

import com.contextos.retrieval.hybrid.CandidateChunk;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Component
public class DeterministicReranker {

    private static final double WEIGHT_SEMANTIC = 0.55;
    private static final double WEIGHT_KEYWORD = 0.25;
    private static final double WEIGHT_FRESHNESS = 0.10;
    private static final double WEIGHT_SOURCE_PRIORITY = 0.10;

    public List<RankedChunk> rerank(List<CandidateChunk> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        return candidates.stream()
                .map(this::calculateScore)
                .sorted(Comparator.comparingDouble(RankedChunk::finalScore).reversed())
                .toList();
    }

    private RankedChunk calculateScore(CandidateChunk candidate) {
        double semantic = Math.max(0.0, Math.min(1.0, candidate.vectorSimilarity()));
        double keyword = Math.max(0.0, Math.min(1.0, candidate.keywordRank()));
        double freshness = calculateFreshnessScore(candidate.updatedAt());
        double priority = calculateSourcePriority(candidate.sourceType());

        double finalScore = (WEIGHT_SEMANTIC * semantic) +
                            (WEIGHT_KEYWORD * keyword) +
                            (WEIGHT_FRESHNESS * freshness) +
                            (WEIGHT_SOURCE_PRIORITY * priority);

        return new RankedChunk(candidate, finalScore, semantic, keyword, freshness, priority);
    }

    private double calculateFreshnessScore(OffsetDateTime updatedAt) {
        if (updatedAt == null) return 0.5;
        long daysOld = Math.abs(Duration.between(updatedAt, OffsetDateTime.now()).toDays());
        if (daysOld <= 1) return 1.0;
        if (daysOld <= 7) return 0.9;
        if (daysOld <= 30) return 0.7;
        if (daysOld <= 90) return 0.5;
        return 0.3;
    }

    private double calculateSourcePriority(String sourceType) {
        if (sourceType == null) return 0.5;
        return switch (sourceType.toUpperCase()) {
            case "GITHUB" -> 0.9;
            case "JIRA" -> 0.85;
            case "LOCAL" -> 0.8;
            default -> 0.7;
        };
    }
}
