package com.contextos.connector;

import java.time.OffsetDateTime;

public record ConnectorHealth(
    boolean isHealthy,
    String message,
    OffsetDateTime checkedAt
) {
    public static ConnectorHealth healthy(String message) {
        return new ConnectorHealth(true, message, OffsetDateTime.now());
    }

    public static ConnectorHealth unhealthy(String message) {
        return new ConnectorHealth(false, message, OffsetDateTime.now());
    }
}
