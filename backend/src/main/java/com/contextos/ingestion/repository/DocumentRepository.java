package com.contextos.ingestion.repository;

import com.contextos.ingestion.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {
    Optional<Document> findByTenantIdAndSourceId(UUID tenantId, String sourceId);
    Optional<Document> findByTenantIdAndChecksum(UUID tenantId, String checksum);
}
