package com.contextos.security.acl;

import com.contextos.retrieval.hybrid.CandidateChunk;
import com.contextos.security.model.AclEntry;
import com.contextos.security.repository.AclEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AclRetrievalFilter {

    private final AclEntryRepository aclEntryRepository;

    public List<CandidateChunk> filterAuthorized(UUID tenantId, String userId, Set<String> userGroups, List<CandidateChunk> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return List.of();
        }

        List<AclEntry> tenantAcls = aclEntryRepository.findByTenantId(tenantId);
        if (tenantAcls.isEmpty()) {
            // If no explicit ACL restrictions exist for tenant, all chunks are accessible by default
            return candidates;
        }

        List<CandidateChunk> authorized = new ArrayList<>();

        for (CandidateChunk candidate : candidates) {
            boolean hasPermission = checkChunkPermission(candidate, userId, userGroups, tenantAcls);
            if (hasPermission) {
                authorized.add(candidate);
            }
        }

        return authorized;
    }

    private boolean checkChunkPermission(CandidateChunk chunk, String userId, Set<String> userGroups, List<AclEntry> tenantAcls) {
        List<AclEntry> relevantAcls = tenantAcls.stream()
                .filter(acl -> (acl.getChunk() != null && acl.getChunk().getId().equals(chunk.chunkId())) ||
                               (acl.getDocument() != null && acl.getDocument().getId().equals(chunk.documentId())))
                .toList();

        if (relevantAcls.isEmpty()) {
            return true; // No ACL restrictions on this chunk
        }

        for (AclEntry acl : relevantAcls) {
            if ("USER".equalsIgnoreCase(acl.getSubjectType()) && acl.getSubjectId().equalsIgnoreCase(userId)) {
                return true;
            }
            if ("GROUP".equalsIgnoreCase(acl.getSubjectType()) && userGroups != null && userGroups.contains(acl.getSubjectId())) {
                return true;
            }
            if ("PUBLIC".equalsIgnoreCase(acl.getSubjectType())) {
                return true;
            }
        }

        return false;
    }
}
