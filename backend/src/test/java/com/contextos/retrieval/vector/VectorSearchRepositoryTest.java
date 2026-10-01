package com.contextos.retrieval.vector;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VectorSearchRepositoryTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query nativeQuery;

    @InjectMocks
    private VectorSearchRepository vectorSearchRepository;

    @Test
    void searchSimilarVectors_validParams_executesQuery() {
        UUID tenantId = UUID.randomUUID();
        float[] vector = new float[]{0.1f, 0.2f, 0.3f};

        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(List.of());

        List<VectorSearchResult> results = vectorSearchRepository.searchSimilarVectors(tenantId, vector, 5);

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(entityManager).createNativeQuery(anyString());
    }
}
