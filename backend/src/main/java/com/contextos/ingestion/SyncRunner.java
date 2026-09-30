package com.contextos.ingestion;

import com.contextos.common.SourceItem;
import com.contextos.connector.ContextConnector;

import com.contextos.connector.model.Connector;
import com.contextos.connector.repository.ConnectorRepository;
import com.contextos.ingestion.model.SyncRun;
import com.contextos.ingestion.repository.SyncRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SyncRunner {

    private final ConnectorRepository connectorRepository;
    private final SyncRunRepository syncRunRepository;
    private final List<ContextConnector> connectors;

    @Async
    public SyncRun runSync(UUID connectorId) {
        Connector connectorEntity = connectorRepository.findById(connectorId)
                .orElseThrow(() -> new IllegalArgumentException("Connector not found: " + connectorId));

        SyncRun syncRun = SyncRun.builder()
                .connector(connectorEntity)
                .tenant(connectorEntity.getTenant())
                .startedAt(OffsetDateTime.now())
                .status("RUNNING")
                .itemsProcessed(0)
                .build();

        syncRun = syncRunRepository.save(syncRun);

        try {
            ContextConnector activeConnector = connectors.stream()
                    .filter(c -> c.type().name().equalsIgnoreCase(connectorEntity.getType()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No connector bean found for type: " + connectorEntity.getType()));

            List<SourceItem> items = activeConnector.fetch(
                    connectorEntity.getTenant().getId(),
                    connectorEntity.getConfigJson() != null ? connectorEntity.getConfigJson() : "{}"
            );

            syncRun.setStatus("SUCCESS");
            syncRun.setCompletedAt(OffsetDateTime.now());
            syncRun.setItemsProcessed(items.size());
            connectorEntity.setLastSyncAt(OffsetDateTime.now());
            connectorEntity.setStatus("ACTIVE");

            connectorRepository.save(connectorEntity);
        } catch (Exception e) {
            syncRun.setStatus("FAILED");
            syncRun.setCompletedAt(OffsetDateTime.now());
            syncRun.setErrorLog(e.getMessage());
        }

        return syncRunRepository.save(syncRun);
    }
}
