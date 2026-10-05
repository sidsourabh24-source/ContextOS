package com.contextos.api.controller;

import com.contextos.api.dto.ContextSearchRequest;
import com.contextos.api.dto.ContextSearchResponse;
import com.contextos.auth.TenantContext;
import com.contextos.common.ContextPackage;
import com.contextos.retrieval.context.ContextAssembler;
import com.contextos.retrieval.hybrid.HybridSearchMerger;
import com.contextos.retrieval.reranker.DeterministicReranker;
import com.contextos.security.acl.AclRetrievalFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContextSearchControllerTest {

    @Mock
    private HybridSearchMerger hybridSearchMerger;

    @Mock
    private AclRetrievalFilter aclRetrievalFilter;

    @Mock
    private DeterministicReranker deterministicReranker;

    @Mock
    private ContextAssembler contextAssembler;

    @InjectMocks
    private ContextSearchController contextSearchController;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        TenantContext.setTenantId(tenantId);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void searchContext_authenticatedTenant_returnsContextPackage() {
        ContextSearchRequest request = ContextSearchRequest.builder()
                .query("Why did Apollo release fail?")
                .topK(5)
                .tokenBudget(2000)
                .build();

        ContextPackage mockPackage = new ContextPackage(
                "ctx_123", request.getQuery(), OffsetDateTime.now(), List.of(), "<retrieved_document></retrieved_document>", 50, null
        );

        when(hybridSearchMerger.searchAndMerge(eq(tenantId), anyString(), anyInt())).thenReturn(List.of());
        when(aclRetrievalFilter.filterAuthorized(eq(tenantId), any(), any(), any())).thenReturn(List.of());
        when(deterministicReranker.rerank(any())).thenReturn(List.of());
        when(contextAssembler.assembleContext(anyString(), any(), anyInt(), anyLong())).thenReturn(mockPackage);

        ResponseEntity<ContextSearchResponse> response = contextSearchController.searchContext(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("ctx_123", response.getBody().getContextPackage().requestId());
    }
}
