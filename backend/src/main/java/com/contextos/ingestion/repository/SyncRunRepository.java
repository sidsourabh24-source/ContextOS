package com.contextos.ingestion.repository;

import com.contextos.ingestion.model.SyncRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SyncRunRepository extends JpaRepository<SyncRun, UUID> {
    List<SyncRun> findByConnectorIdOrderByStartedAtDesc(UUID connectorId);
    List<SyncRun> findByTenantId(UUID tenantId);
}
