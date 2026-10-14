package com.contextos.retrieval.vector;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class VectorSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<VectorSearchResult> searchSimilarVectors(UUID tenantId, float[] queryVector, int limit) {
        if (tenantId == null || queryVector == null || queryVector.length == 0) {
            return List.of();
        }

        String vectorString = formatVector(queryVector);
        String sql = """
            SELECT c.id AS chunk_id, c.document_id, c.tenant_id, c.content, 
                   (1 - (c.embedding <=> CAST(:vector AS vector))) AS similarity,
                   c.chunk_index, d.title, d.url
            FROM chunks c
            JOIN documents d ON c.document_id = d.id
            WHERE c.tenant_id = :tenantId
              AND c.embedding IS NOT NULL
            ORDER BY c.embedding <=> CAST(:vector AS vector) ASC
            LIMIT :limit
            """;

        Query nativeQuery = entityManager.createNativeQuery(sql)
                .setParameter("tenantId", tenantId)
                .setParameter("vector", vectorString)
                .setParameter("limit", limit);

        List<Object[]> rows = nativeQuery.getResultList();
        List<VectorSearchResult> results = new ArrayList<>();

        for (Object[] row : rows) {
            results.add(new VectorSearchResult(
                    (UUID) row[0],
                    (UUID) row[1],
                    (UUID) row[2],
                    (String) row[3],
                    ((Number) row[4]).doubleValue(),
                    ((Number) row[5]).intValue(),
                    (String) row[6],
                    (String) row[7]
            ));
        }

        return results;
    }

    private String formatVector(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
