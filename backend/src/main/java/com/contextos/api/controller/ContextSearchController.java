package com.contextos.api.controller;

import com.contextos.analytics.RetrievalTracer;
import com.contextos.api.dto.ContextSearchRequest;
import com.contextos.api.dto.ContextSearchResponse;
import com.contextos.auth.TenantContext;
import com.contextos.common.ContextPackage;
import com.contextos.common.cache.RedisCacheManager;
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
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/context")
@RequiredArgsConstructor
public class ContextSearchController {

    private final HybridSearchMerger hybridSearchMerger;
    private final AclRetrievalFilter aclRetrievalFilter;
    private final DeterministicReranker deterministicReranker;
    private final ContextAssembler contextAssembler;
    private final RedisCacheManager redisCacheManager;
    private final RetrievalTracer retrievalTracer;

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

        String userId = tenantId.toString();
        String userScope = (request.getUserGroups() != null && !request.getUserGroups().isEmpty())
                ? String.join("_", request.getUserGroups().stream().sorted().toList())
                : "DEFAULT";
        String cacheUserKey = tenantId + ":" + userScope;

        // 1. Check Redis Cache (Role-scoped to prevent privilege leakage)
        Optional<ContextSearchResponse> cachedSearch = redisCacheManager.getCachedSearch(tenantId, cacheUserKey, request.getQuery());
        if (cachedSearch.isPresent()) {
            ContextSearchResponse cachedResp = cachedSearch.get();
            if (cachedResp.getTraceDetails() != null) {
                cachedResp.getTraceDetails().setFromCache(true);
            }
            return ResponseEntity.ok(cachedResp);
        }

        // 2. Hybrid Search
        List<CandidateChunk> candidates = hybridSearchMerger.searchAndMerge(tenantId, request.getQuery(), request.getTopK());

        // 3. ACL Authorization Filter (At retrieval time!)
        List<CandidateChunk> authorized = aclRetrievalFilter.filterAuthorized(
                tenantId,
                userId,
                request.getUserGroups(),
                candidates
        );

        List<String> strippedTitles = candidates.stream()
                .filter(c -> !authorized.contains(c))
                .map(CandidateChunk::documentTitle)
                .filter(t -> t != null && !t.isBlank())
                .distinct()
                .toList();

        // 4. Deterministic Reranker
        List<RankedChunk> ranked = deterministicReranker.rerank(authorized);

        // 5. Token-Budgeted Context Package Assembly
        long latencyMs = System.currentTimeMillis() - startTime;
        ContextPackage contextPackage = contextAssembler.assembleContext(
                request.getQuery(),
                ranked,
                request.getTokenBudget(),
                latencyMs
        );

        ContextSearchResponse.TraceDetails trace = ContextSearchResponse.TraceDetails.builder()
                .totalCandidatesCount(candidates.size())
                .aclAuthorizedCount(authorized.size())
                .aclFilteredCount(candidates.size() - authorized.size())
                .fromCache(false)
                .strippedTitles(strippedTitles)
                .build();

        ContextSearchResponse response = ContextSearchResponse.builder()
                .success(true)
                .contextPackage(contextPackage)
                .traceDetails(trace)
                .build();

        // 6. Write to Cache and Telemetry
        redisCacheManager.cacheSearch(tenantId, cacheUserKey, request.getQuery(), response);
        retrievalTracer.recordTrace(tenantId, request.getQuery(), request.getTopK(), latencyMs);

        return ResponseEntity.ok(response);
    }
}
