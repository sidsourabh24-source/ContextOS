package com.contextos.ingestion;

import com.contextos.common.SourceItem;
import com.contextos.connector.ConnectorType;
import com.contextos.connector.ContextConnector;
import com.contextos.connector.model.Connector;
import com.contextos.connector.repository.ConnectorRepository;
import com.contextos.ingestion.model.SyncRun;
import com.contextos.ingestion.repository.SyncRunRepository;
import com.contextos.tenant.model.Tenant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SyncRunnerTest {

    @Mock
    private ConnectorRepository connectorRepository;

    @Mock
    private SyncRunRepository syncRunRepository;

    @Mock
    private ContextConnector mockConnector;

    @Test
    void runSync_validConnector_success() {
        UUID tenantId = UUID.randomUUID();
        UUID connectorId = UUID.randomUUID();

        Tenant tenant = Tenant.builder().id(tenantId).name("Test Tenant").build();
        Connector connector = Connector.builder()
                .id(connectorId)
                .tenant(tenant)
                .type("LOCAL")
                .name("Local Docs")
                .configJson("{\"path\":\"/tmp\"}")
                .build();

        SourceItem item = new SourceItem(
                "id-1", ConnectorType.LOCAL, "url", "Title", "Content",
                "author", OffsetDateTime.now(), OffsetDateTime.now(), "text/plain",
                tenantId, "checksum", Map.of()
        );

        when(connectorRepository.findById(connectorId)).thenReturn(Optional.of(connector));
        when(mockConnector.type()).thenReturn(ConnectorType.LOCAL);
        when(mockConnector.fetch(eq(tenantId), any())).thenReturn(List.of(item));
        when(syncRunRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SyncRunner syncRunner = new SyncRunner(connectorRepository, syncRunRepository, List.of(mockConnector));

        SyncRun result = syncRunner.runSync(connectorId);

        assertEquals("SUCCESS", result.getStatus());
        assertEquals(1, result.getItemsProcessed());
    }
}
