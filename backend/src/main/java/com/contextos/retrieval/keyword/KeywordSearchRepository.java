package com.contextos.retrieval.keyword;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class KeywordSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<KeywordSearchResult> searchByKeywords(UUID tenantId, String searchQuery, int limit) {
        if (tenantId == null || searchQuery == null || searchQuery.isBlank()) {
            return List.of();
        }

        String sql = """
            SELECT c.id AS chunk_id, c.document_id, c.tenant_id, c.content,
                   ts_rank(to_tsvector('english', c.content), plainto_tsquery('english', :query)) AS rank,
                   c.chunk_index, d.title, d.url
            FROM chunks c
            JOIN documents d ON c.document_id = d.id
            WHERE c.tenant_id = :tenantId
              AND to_tsvector('english', c.content) @@ plainto_tsquery('english', :query)
            ORDER BY rank DESC
            LIMIT :limit
            """;

        Query nativeQuery = entityManager.createNativeQuery(sql)
                .setParameter("tenantId", tenantId)
                .setParameter("query", searchQuery.trim())
                .setParameter("limit", limit);

        List<Object[]> rows = nativeQuery.getResultList();
        List<KeywordSearchResult> results = new ArrayList<>();

        for (Object[] row : rows) {
            results.add(new KeywordSearchResult(
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
}
