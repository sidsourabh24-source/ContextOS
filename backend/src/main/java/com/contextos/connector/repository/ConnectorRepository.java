package com.contextos.connector.repository;

import com.contextos.connector.model.Connector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConnectorRepository extends JpaRepository<Connector, UUID> {
    List<Connector> findByTenantId(UUID tenantId);
    List<Connector> findByTenantIdAndType(UUID tenantId, String type);
}
