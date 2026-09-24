package com.contextos.security.model;

import com.contextos.ingestion.model.Chunk;
import com.contextos.ingestion.model.Document;
import com.contextos.tenant.model.Tenant;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "acl_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AclEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chunk_id")
    private Chunk chunk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Column(name = "subject_type", nullable = false)
    private String subjectType; // USER, GROUP, ROLE

    @Column(name = "subject_id", nullable = false)
    private String subjectId;

    @Column(nullable = false)
    @Builder.Default
    private String permission = "READ";
}
