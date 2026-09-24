package com.contextos.connector.model;

import com.contextos.tenant.model.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "connectors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Connector {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(nullable = false)
    private String type; // GITHUB, LOCAL, JIRA, SLACK

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Builder.Default
    private String status = "INACTIVE";

    @Column(name = "config_json", columnDefinition = "TEXT")
    private String configJson;

    @Column(name = "last_sync_at")
    private OffsetDateTime lastSyncAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
