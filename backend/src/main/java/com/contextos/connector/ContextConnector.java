package com.contextos.connector;

import com.contextos.common.SourceItem;

import java.util.List;
import java.util.UUID;

public interface ContextConnector {

    ConnectorType type();

    void validateConnection();

    ConnectorHealth health();

    List<SourceItem> fetch(UUID tenantId, String configJson);
}
