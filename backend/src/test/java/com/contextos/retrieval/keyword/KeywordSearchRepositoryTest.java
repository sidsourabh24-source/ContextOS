package com.contextos.retrieval.keyword;

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
class KeywordSearchRepositoryTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query nativeQuery;

    @InjectMocks
    private KeywordSearchRepository keywordSearchRepository;

    @Test
    void searchByKeywords_validQuery_executesQuery() {
        UUID tenantId = UUID.randomUUID();

        when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
        when(nativeQuery.setParameter(anyString(), any())).thenReturn(nativeQuery);
        when(nativeQuery.getResultList()).thenReturn(List.of());

        List<KeywordSearchResult> results = keywordSearchRepository.searchByKeywords(tenantId, "APOLLO-421 failure", 5);

        assertNotNull(results);
        assertTrue(results.isEmpty());
        verify(entityManager).createNativeQuery(anyString());
    }
}
