package com.contextos.connector.local;

import com.contextos.common.SourceItem;
import com.contextos.connector.ConnectorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LocalFileConnectorTest {

    private final LocalFileConnector connector = new LocalFileConnector();

    @Test
    void fetch_validDirectory_returnsSourceItems(@TempDir Path tempDir) throws IOException {
        Path file1 = tempDir.resolve("doc1.md");
        Files.writeString(file1, "# Hello World Content");

        Path file2 = tempDir.resolve("doc2.txt");
        Files.writeString(file2, "Sample plain text");

        UUID tenantId = UUID.randomUUID();
        String configJson = String.format("{\"path\": \"%s\"}", tempDir.toAbsolutePath().toString().replace("\\", "\\\\"));

        List<SourceItem> items = connector.fetch(tenantId, configJson);

        assertEquals(2, items.size());
        assertEquals(ConnectorType.LOCAL, items.get(0).sourceType());
        assertEquals(tenantId, items.get(0).tenantId());
        assertNotNull(items.get(0).checksum());
    }
}
