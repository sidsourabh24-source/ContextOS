package com.contextos.security.repository;

import com.contextos.security.model.AclEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AclEntryRepository extends JpaRepository<AclEntry, UUID> {
    List<AclEntry> findByTenantId(UUID tenantId);
    List<AclEntry> findByChunkId(UUID chunkId);
    List<AclEntry> findByDocumentId(UUID documentId);
}
