package com.contextos.connector.github;

import com.contextos.common.SourceItem;
import com.contextos.connector.ConnectorType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GitHubConnectorTest {

    private final GitHubConnector connector = new GitHubConnector();

    @Test
    void fetch_validConfig_returnsNormalizedSourceItem() {
        UUID tenantId = UUID.randomUUID();
        String configJson = "{\"owner\": \"octocat\", \"repo\": \"Hello-World\"}";

        List<SourceItem> items = connector.fetch(tenantId, configJson);

        assertFalse(items.isEmpty());
        assertEquals(ConnectorType.GITHUB, items.get(0).sourceType());
        assertEquals(tenantId, items.get(0).tenantId());
        assertTrue(items.get(0).sourceUrl().contains("octocat/Hello-World"));
    }
}
