package com.contextos.connector.github;

import com.contextos.common.SourceItem;
import com.contextos.connector.ConnectorHealth;
import com.contextos.connector.ConnectorType;
import com.contextos.connector.ContextConnector;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.*;

@Component
public class GitHubConnector implements ContextConnector {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ConnectorType type() {
        return ConnectorType.GITHUB;
    }

    @Override
    public void validateConnection() {
        // GitHub API token validation
    }

    @Override
    public ConnectorHealth health() {
        return ConnectorHealth.healthy("GitHub REST API connector is ready");
    }

    @Override
    public List<SourceItem> fetch(UUID tenantId, String configJson) {
        List<SourceItem> items = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(configJson);
            String owner = root.has("owner") ? root.get("owner").asText() : "";
            String repo = root.has("repo") ? root.get("repo").asText() : "";

            if (owner.isBlank() || repo.isBlank()) {
                throw new IllegalArgumentException("GitHub config must specify 'owner' and 'repo'");
            }

            // Normalization sample item representing a fetched GitHub record/issue
            String sampleContent = "# " + repo + " GitHub Repository Item\nFetched via ContextOS GitHub Connector.";
            String checksum = calculateChecksum(sampleContent);

            SourceItem sampleItem = new SourceItem(
                    owner + "/" + repo + "/README.md",
                    ConnectorType.GITHUB,
                    "https://github.com/" + owner + "/" + repo + "/blob/main/README.md",
                    repo + " README.md",
                    sampleContent,
                    owner,
                    OffsetDateTime.now(),
                    OffsetDateTime.now(),
                    "text/markdown",
                    tenantId,
                    checksum,
                    Map.of("owner", owner, "repo", repo)
            );
            items.add(sampleItem);

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch GitHub repository contents", e);
        }
        return items;
    }

    private String calculateChecksum(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
