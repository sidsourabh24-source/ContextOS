package com.contextos.analytics.repository;

import com.contextos.analytics.model.RetrievalRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RetrievalRequestLogRepository extends JpaRepository<RetrievalRequestLog, UUID> {
    List<RetrievalRequestLog> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
}
