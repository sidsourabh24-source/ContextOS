package com.contextos.api.controller;

import com.contextos.api.dto.ContextSearchRequest;
import com.contextos.api.dto.ContextSearchResponse;
import com.contextos.auth.TenantContext;
import com.contextos.common.ContextPackage;
import com.contextos.retrieval.context.ContextAssembler;
import com.contextos.retrieval.hybrid.CandidateChunk;
import com.contextos.retrieval.hybrid.HybridSearchMerger;
import com.contextos.retrieval.reranker.DeterministicReranker;
import com.contextos.retrieval.reranker.RankedChunk;
import com.contextos.security.acl.AclRetrievalFilter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/context")
@RequiredArgsConstructor
public class ContextSearchController {

    private final HybridSearchMerger hybridSearchMerger;
    private final AclRetrievalFilter aclRetrievalFilter;
    private final DeterministicReranker deterministicReranker;
    private final ContextAssembler contextAssembler;

    @PostMapping("/search")
    public ResponseEntity<ContextSearchResponse> searchContext(@Valid @RequestBody ContextSearchRequest request) {
        long startTime = System.currentTimeMillis();
        UUID tenantId = TenantContext.getTenantId();

        if (tenantId == null) {
            return ResponseEntity.status(401).body(ContextSearchResponse.builder()
                    .success(false)
                    .error("Unauthorized: Tenant Context missing")
                    .build());
        }

        // 1. Hybrid Search
        List<CandidateChunk> candidates = hybridSearchMerger.searchAndMerge(tenantId, request.getQuery(), request.getTopK());

        // 2. ACL Authorization Filter (At retrieval time!)
        List<CandidateChunk> authorized = aclRetrievalFilter.filterAuthorized(
                tenantId,
                tenantId.toString(),
                request.getUserGroups(),
                candidates
        );

        // 3. Deterministic Reranker
        List<RankedChunk> ranked = deterministicReranker.rerank(authorized);

        // 4. Token-Budgeted Context Package Assembly
        long latencyMs = System.currentTimeMillis() - startTime;
        ContextPackage contextPackage = contextAssembler.assembleContext(
                request.getQuery(),
                ranked,
                request.getTokenBudget(),
                latencyMs
        );

        return ResponseEntity.ok(ContextSearchResponse.builder()
                .success(true)
                .contextPackage(contextPackage)
                .build());
    }
}
