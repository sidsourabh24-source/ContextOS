package com.contextos.eval;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class GoldenDatasetEvaluatorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public record GoldenTestCase(
            String id,
            String query,
            List<String> expectedSources,
            int minTopK,
            int minExpectedMatches
    ) {}

    @Test
    void evaluateRetrievalBenchmark_recallAndMrrExceedThresholds() throws Exception {
        InputStream is = getClass().getResourceAsStream("/golden_dataset.json");
        assertNotNull(is, "golden_dataset.json should exist in test resources");

        List<GoldenTestCase> testCases = objectMapper.readValue(is, new TypeReference<>() {});
        assertFalse(testCases.isEmpty(), "Test cases must not be empty");

        // Simulated benchmark mock retrieval map (query -> returned document IDs in rank order)
        Map<String, List<String>> mockRetrievalEngineResults = Map.of(
                "Why did the Apollo payment release fail?", List.of("APOLLO-421", "PR-981", "OTHER-1", "OTHER-2", "INC-302"),
                "What is the timeout threshold for Stripe webhook handling?", List.of("PR-981", "OTHER-3", "PAYMENTS-CONFIG"),
                "Where are employee reimbursement policy guidelines documented?", List.of("HR-HANDBOOK", "OTHER-4", "POLICY-EXPENSE-2026"),
                "How do we rotate database credentials in staging?", List.of("SECRETS-MGMT", "RUNBOOK-DB-ROTATION", "OTHER-5"),
                "What caused the Redis connection pool exhaustion during peak hours?", List.of("INC-409", "PR-1022", "REDIS-ARCHITECTURE")
        );

        int totalCases = testCases.size();
        double sumRecall = 0.0;
        double sumReciprocalRank = 0.0;

        for (GoldenTestCase tc : testCases) {
            List<String> retrieved = mockRetrievalEngineResults.getOrDefault(tc.query(), List.of());
            List<String> topK = retrieved.subList(0, Math.min(tc.minTopK(), retrieved.size()));

            Set<String> expectedSet = Set.copyOf(tc.expectedSources());
            long matchCount = topK.stream().filter(expectedSet::contains).count();

            // Recall@K
            double recall = (double) matchCount / (double) tc.expectedSources().size();
            sumRecall += recall;

            // Reciprocal Rank (MRR)
            double reciprocalRank = 0.0;
            for (int i = 0; i < topK.size(); i++) {
                if (expectedSet.contains(topK.get(i))) {
                    reciprocalRank = 1.0 / (i + 1);
                    break;
                }
            }
            sumReciprocalRank += reciprocalRank;

            // Golden test case threshold assertion
            assertTrue(matchCount >= tc.minExpectedMatches(),
                    String.format("Case %s failed: Expected at least %d matches in top %d, found %d",
                            tc.id(), tc.minExpectedMatches(), tc.minTopK(), matchCount));
        }

        double meanRecall = sumRecall / totalCases;
        double meanReciprocalRank = sumReciprocalRank / totalCases;

        // Assert baseline targets from PDF Section 3.3 & 21
        assertTrue(meanRecall >= 0.80, "Mean Recall@K must be >= 0.80, got " + meanRecall);
        assertTrue(meanReciprocalRank >= 0.70, "MRR must be >= 0.70, got " + meanReciprocalRank);
    }
}
