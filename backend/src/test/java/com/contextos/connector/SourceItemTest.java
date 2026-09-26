package com.contextos.connector;

import com.contextos.common.SourceItem;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SourceItemTest {

    @Test
    void createSourceItem_validData_success() {
        UUID tenantId = UUID.randomUUID();
        SourceItem item = new SourceItem(
                "doc-123",
                ConnectorType.LOCAL,
                "file:///tmp/test.md",
                "Test Title",
                "# Hello World",
                "author@example.com",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "text/markdown",
                tenantId,
                "checksum-abc-123",
                Map.of("key", "value")
        );

        assertEquals("doc-123", item.sourceId());
        assertEquals(ConnectorType.LOCAL, item.sourceType());
        assertEquals(tenantId, item.tenantId());
        assertEquals("value", item.metadata().get("key"));
    }

    @Test
    void createSourceItem_nullSourceId_throwsException() {
        UUID tenantId = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () ->
                new SourceItem(
                        null,
                        ConnectorType.LOCAL,
                        "file:///tmp/test.md",
                        "Test",
                        "Content",
                        "author",
                        OffsetDateTime.now(),
                        OffsetDateTime.now(),
                        "text/plain",
                        tenantId,
                        "checksum-123",
                        Map.of()
                )
        );
    }
}
