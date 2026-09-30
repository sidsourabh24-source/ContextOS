package com.contextos.ingestion.model;

import com.contextos.connector.model.Connector;
import com.contextos.tenant.model.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "sync_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncRun {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "connector_id", nullable = false)
    private Connector connector;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "started_at", nullable = false)
    @Builder.Default
    private OffsetDateTime startedAt = OffsetDateTime.now();

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(nullable = false)
    private String status; // RUNNING, SUCCESS, FAILED

    @Column(name = "items_processed")
    @Builder.Default
    private Integer itemsProcessed = 0;

    @Column(name = "error_log", columnDefinition = "TEXT")
    private String errorLog;
}
