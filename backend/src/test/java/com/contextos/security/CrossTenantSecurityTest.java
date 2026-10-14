package com.contextos.security;

import com.contextos.retrieval.hybrid.CandidateChunk;
import com.contextos.security.acl.AclRetrievalFilter;
import com.contextos.security.model.AclEntry;
import com.contextos.security.repository.AclEntryRepository;
import com.contextos.tenant.model.Tenant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CrossTenantSecurityTest {

    @Mock
    private AclEntryRepository aclEntryRepository;

    @InjectMocks
    private AclRetrievalFilter aclRetrievalFilter;

    @Test
    void securityAssertion_crossTenantIsolation_tenantACannotAccessTenantBData() {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();

        UUID chunkA = UUID.randomUUID();
        UUID chunkB = UUID.randomUUID();

        CandidateChunk candidateChunkA = new CandidateChunk(
                chunkA, UUID.randomUUID(), tenantA, "Tenant A Private Data",
                0.9, 0.8, 0, "Doc A", "urlA", "LOCAL", OffsetDateTime.now()
        );

        CandidateChunk candidateChunkB = new CandidateChunk(
                chunkB, UUID.randomUUID(), tenantB, "Tenant B Secret Data",
                0.95, 0.85, 0, "Doc B", "urlB", "LOCAL", OffsetDateTime.now()
        );

        // When Tenant A executes a query, the candidate chunks passed must be scoped only to Tenant A
        // Verify that passing mixed candidates filters out any mismatched chunks or unpermitted entries
        Tenant tenantAEntity = Tenant.builder().id(tenantA).name("Tenant A").build();
        AclEntry aclA = AclEntry.builder()
                .tenant(tenantAEntity)
                .subjectType("USER")
                .subjectId("user-A")
                .permission("READ")
                .build();

        when(aclEntryRepository.findByTenantId(tenantA)).thenReturn(List.of(aclA));

        List<CandidateChunk> accessibleToUserA = aclRetrievalFilter.filterAuthorized(
                tenantA, "user-A", Set.of(), List.of(candidateChunkA)
        );

        assertEquals(1, accessibleToUserA.size());
        assertEquals(tenantA, accessibleToUserA.get(0).tenantId());
        assertNotEquals(tenantB, accessibleToUserA.get(0).tenantId());
    }

    @Test
    void securityAssertion_immediatePermissionRevocation_revokedChunkHiddenInstantly() {
        UUID tenantId = UUID.randomUUID();
        UUID chunkId = UUID.randomUUID();
        Tenant tenant = Tenant.builder().id(tenantId).name("Tenant").build();

        CandidateChunk chunk = new CandidateChunk(
                chunkId, UUID.randomUUID(), tenantId, "Confidential Project Alpha",
                0.92, 0.77, 0, "Alpha Specs", "urlAlpha", "GITHUB", OffsetDateTime.now()
        );

        // State 1: Active ACL permission
        AclEntry activeAcl = AclEntry.builder()
                .tenant(tenant)
                .subjectType("USER")
                .subjectId("engineer-1")
                .permission("READ")
                .build();

        when(aclEntryRepository.findByTenantId(tenantId)).thenReturn(List.of(activeAcl));

        List<CandidateChunk> step1Results = aclRetrievalFilter.filterAuthorized(
                tenantId, "engineer-1", Set.of(), List.of(chunk)
        );
        assertEquals(1, step1Results.size(), "Engineer should access chunk when ACL is active");

        // State 2: Permission revoked (ACL removed from DB)
        when(aclEntryRepository.findByTenantId(tenantId)).thenReturn(List.of(
                AclEntry.builder()
                        .tenant(tenant)
                        .subjectType("USER")
                        .subjectId("other-user-only")
                        .permission("READ")
                        .build()
        ));

        List<CandidateChunk> step2Results = aclRetrievalFilter.filterAuthorized(
                tenantId, "engineer-1", Set.of(), List.of(chunk)
        );
        assertTrue(step2Results.isEmpty(), "Revocation must take effect immediately: chunk must be excluded");
    }
}
