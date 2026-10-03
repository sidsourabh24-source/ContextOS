package com.contextos.security.acl;

import com.contextos.ingestion.model.Chunk;
import com.contextos.retrieval.hybrid.CandidateChunk;
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
class AclRetrievalFilterTest {

    @Mock
    private AclEntryRepository aclEntryRepository;

    @InjectMocks
    private AclRetrievalFilter aclRetrievalFilter;

    @Test
    void filterAuthorized_privateChunk_userAuthorized_returnsChunk() {
        UUID tenantId = UUID.randomUUID();
        UUID chunkId = UUID.randomUUID();
        UUID docId = UUID.randomUUID();

        Tenant tenant = Tenant.builder().id(tenantId).name("Test Tenant").build();
        Chunk chunk = Chunk.builder().id(chunkId).tenant(tenant).build();

        CandidateChunk candidate = new CandidateChunk(chunkId, docId, tenantId, "Private Content", 0.9, 0.5, 0, "Title", "url", "DOC", OffsetDateTime.now());

        AclEntry acl = AclEntry.builder()
                .tenant(tenant)
                .chunk(chunk)
                .subjectType("USER")
                .subjectId("user-123")
                .permission("READ")
                .build();

        when(aclEntryRepository.findByTenantId(tenantId)).thenReturn(List.of(acl));

        List<CandidateChunk> authorized = aclRetrievalFilter.filterAuthorized(tenantId, "user-123", Set.of("engineering"), List.of(candidate));

        assertEquals(1, authorized.size());
    }

    @Test
    void filterAuthorized_privateChunk_unauthorizedUser_filtersOutChunk() {
        UUID tenantId = UUID.randomUUID();
        UUID chunkId = UUID.randomUUID();
        UUID docId = UUID.randomUUID();

        Tenant tenant = Tenant.builder().id(tenantId).name("Test Tenant").build();
        Chunk chunk = Chunk.builder().id(chunkId).tenant(tenant).build();

        CandidateChunk candidate = new CandidateChunk(chunkId, docId, tenantId, "Private Content", 0.9, 0.5, 0, "Title", "url", "DOC", OffsetDateTime.now());

        AclEntry acl = AclEntry.builder()
                .tenant(tenant)
                .chunk(chunk)
                .subjectType("USER")
                .subjectId("user-allowed-only")
                .permission("READ")
                .build();

        when(aclEntryRepository.findByTenantId(tenantId)).thenReturn(List.of(acl));

        List<CandidateChunk> authorized = aclRetrievalFilter.filterAuthorized(tenantId, "user-denied", Set.of("guest"), List.of(candidate));

        assertTrue(authorized.isEmpty());
    }
}
