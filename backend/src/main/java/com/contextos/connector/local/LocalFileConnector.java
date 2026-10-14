package com.contextos.connector.local;

import com.contextos.common.SourceItem;
import com.contextos.connector.ConnectorHealth;
import com.contextos.connector.ConnectorType;
import com.contextos.connector.ContextConnector;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Stream;

@Component
public class LocalFileConnector implements ContextConnector {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ConnectorType type() {
        return ConnectorType.LOCAL;
    }

    @Override
    public void validateConnection() {
        // Local connector validation
    }

    @Override
    public ConnectorHealth health() {
        return ConnectorHealth.healthy("Local file system connector is active");
    }

    @Override
    public List<SourceItem> fetch(UUID tenantId, String configJson) {
        List<SourceItem> items = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(configJson);
            String pathString = root.has("path") ? root.get("path").asText() : "";
            if (pathString.isBlank()) {
                throw new IllegalArgumentException("Config missing 'path' property");
            }

            Path rootPath = Paths.get(pathString);
            if (!Files.exists(rootPath)) {
                throw(new IllegalArgumentException("Path does not exist: " + pathString));
            }

            try (Stream<Path> paths = Files.walk(rootPath)) {
                paths.filter(Files::isRegularFile)
                     .filter(this::isSupportedExtension)
                     .forEach(filePath -> {
                         try {
                             String content = Files.readString(filePath, StandardCharsets.UTF_8);
                             String checksum = calculateChecksum(content);
                             OffsetDateTime modifiedTime = OffsetDateTime.ofInstant(
                                     Files.getLastModifiedTime(filePath).toInstant(), ZoneOffset.UTC
                             );

                             SourceItem item = new SourceItem(
                                     filePath.toAbsolutePath().toString(),
                                     ConnectorType.LOCAL,
                                     filePath.toUri().toString(),
                                     filePath.getFileName().toString(),
                                     content,
                                     "local-user",
                                     modifiedTime,
                                     modifiedTime,
                                     getMimeType(filePath),
                                     tenantId,
                                     checksum,
                                     Map.of("fileSize", Files.size(filePath))
                             );
                             items.add(item);
                         } catch (IOException e) {
                             // Log or skip individual unreadable files
                         }
                     });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse connector config or read directory", e);
        }
        return items;
    }

    private boolean isSupportedExtension(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        return name.endsWith(".md") || name.endsWith(".txt") || name.endsWith(".json") || name.endsWith(".pdf");
    }

    private String getMimeType(Path path) {
        String name = path.getFileName().toString().toLowerCase();
        if (name.endsWith(".md")) return "text/markdown";
        if (name.endsWith(".json")) return "application/json";
        if (name.endsWith(".pdf")) return "application/pdf";
        return "text/plain";
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
