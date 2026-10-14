package com.contextos.common;

import com.contextos.auth.model.ApiKey;
import com.contextos.auth.model.User;
import com.contextos.auth.repository.ApiKeyRepository;
import com.contextos.auth.repository.UserRepository;
import com.contextos.connector.ConnectorType;
import com.contextos.connector.model.Connector;
import com.contextos.connector.repository.ConnectorRepository;
import com.contextos.ingestion.embedding.EmbeddingService;
import com.contextos.ingestion.model.Document;
import com.contextos.ingestion.repository.DocumentRepository;
import com.contextos.security.model.AclEntry;
import com.contextos.security.repository.AclEntryRepository;
import com.contextos.tenant.model.Tenant;
import com.contextos.tenant.repository.TenantRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final ConnectorRepository connectorRepository;
    private final DocumentRepository documentRepository;
    private final AclEntryRepository aclEntryRepository;
    private final EmbeddingService embeddingService;

    @PersistenceContext
    private EntityManager entityManager;

    public static final String DEMO_API_KEY = "ctx-live-test-key-12345";
    public static final UUID DEMO_TENANT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Override
    @Transactional
    public void run(String... args) {
        if (tenantRepository.count() > 0) {
            log.info("Database already seeded. Skipping DevDataSeeder.");
            return;
        }

        log.info("Seeding initial enterprise demo data for ContextOS...");

        // 1. Create Tenant
        Tenant tenant = Tenant.builder()
                .id(DEMO_TENANT_ID)
                .name("Acme Enterprise Corp")
                .plan("ENTERPRISE")
                .build();
        tenant = tenantRepository.save(tenant);

        // 2. Create User
        User user = User.builder()
                .tenant(tenant)
                .email("admin@acme.com")
                .role("ADMIN")
                .status("ACTIVE")
                .build();
        userRepository.save(user);

        // 3. Create Demo API Key
        String keyHash = hashKey(DEMO_API_KEY);
        ApiKey apiKey = ApiKey.builder()
                .tenant(tenant)
                .name("Default Demo API Key")
                .keyHash(keyHash)
                .status("ACTIVE")
                .build();
        apiKeyRepository.save(apiKey);

        // 4. Create Connectors
        Connector githubConnector = Connector.builder()
                .tenant(tenant)
                .name("Acme GitHub Repositories")
                .type(ConnectorType.GITHUB.name())
                .status("ACTIVE")
                .configJson("{\"repo\": \"acme/platform-core\"}")
                .lastSyncAt(OffsetDateTime.now())
                .build();
        githubConnector = connectorRepository.save(githubConnector);

        Connector localConnector = Connector.builder()
                .tenant(tenant)
                .name("Local Engineering Runbooks")
                .type(ConnectorType.LOCAL.name())
                .status("ACTIVE")
                .configJson("{\"path\": \"/var/docs/engineering\"}")
                .lastSyncAt(OffsetDateTime.now())
                .build();
        localConnector = connectorRepository.save(localConnector);

        // 5. Create Documents and Chunks with Embeddings
        Document doc1 = createDocument(
                tenant, githubConnector, "doc-apollo-421",
                "APOLLO-421 Postmortem: Payment Gateway Webhook Timeout Incident",
                "https://github.com/acme/incidents/APOLLO-421.md",
                "hash-421"
        );
        createChunkWithVector(doc1, tenant, 0,
                "Incident APOLLO-421: On 2026-03-14, the Stripe payment release webhook experienced a 45-second timeout spike. The root cause was thread pool exhaustion in the billing-service due to synchronous external HTTP calls without backoff. Resolution involved migrating to async event-driven queues with dead letter retry.",
                48
        );

        Document doc2 = createDocument(
                tenant, localConnector, "doc-zero-trust-rfc",
                "RFC-0042: Zero-Trust Context Layer Architecture & RBAC Guidelines",
                "https://github.com/acme/rfcs/0042-zero-trust.md",
                "hash-0042"
        );
        createChunkWithVector(doc2, tenant, 0,
                "ContextOS enforces retrieval-time authorization. Candidate chunks from vector search (<=>) and full-text keyword search are intersected with tenant ACL permissions before entering the deterministic reranker and context assembler. Token budget caps context at 4096 tokens with strict XML boundaries.",
                52
        );

        Document doc3 = createDocument(
                tenant, githubConnector, "doc-stripe-runbook",
                "Runbook: Stripe Webhook Timeout Thresholds & Circuit Breaker",
                "https://github.com/acme/runbooks/stripe-webhooks.md",
                "hash-stripe-rb"
        );
        createChunkWithVector(doc3, tenant, 0,
                "Webhook timeout threshold is strictly 10,000ms. If external payment provider latency exceeds 10s, the circuit breaker opens and transitions payment status to PENDING_ASYNC. Retry interval follows exponential backoff: 2s, 4s, 8s, up to 5 attempts.",
                45
        );

        // Document 4: RESTRICTED CONFIDENTIAL DOCUMENT
        Document doc4 = createDocument(
                tenant, localConnector, "doc-exec-compensation",
                "CONFIDENTIAL: Q4 Executive Compensation & Salary Matrix 2026",
                "https://internal.acme.corp/hr/compensation-2026.md",
                "hash-exec-comp"
        );
        createChunkWithVector(doc4, tenant, 0,
                "CONFIDENTIAL COMPENSATION DATA: Executive Tier 1 base salary scales: VP Engineering: $340,000 + 40% bonus. Chief Product Officer: $360,000 + 45% bonus. Equity grant allocations scheduled for Q4 vesting cycle.",
                42
        );

        // 6. Set ACLs
        // Public/Engineer ACL for Docs 1, 2, 3
        createAcl(doc1, tenant, "GROUP", "ROLE_ENGINEER");
        createAcl(doc2, tenant, "GROUP", "ROLE_ENGINEER");
        createAcl(doc3, tenant, "GROUP", "ROLE_ENGINEER");

        // RESTRICTED ACL for Doc 4 (ONLY ROLE_EXECUTIVE)
        createAcl(doc4, tenant, "GROUP", "ROLE_EXECUTIVE");

        log.info("Enterprise demo data seeded successfully with 4 documents, pgvector embeddings, and ACL rules!");
    }

    private Document createDocument(Tenant tenant, Connector connector, String sourceId, String title, String url, String checksum) {
        Document doc = Document.builder()
                .tenant(tenant)
                .connector(connector)
                .sourceId(sourceId)
                .title(title)
                .url(url)
                .checksum(checksum)
                .updatedAt(OffsetDateTime.now())
                .build();
        return documentRepository.save(doc);
    }

    private void createChunkWithVector(Document doc, Tenant tenant, int index, String content, int tokenCount) {
        UUID chunkId = UUID.randomUUID();
        float[] vector = embeddingService.embed(content);
        String vectorStr = formatVector(vector);

        String sql = """
            INSERT INTO chunks (id, document_id, tenant_id, chunk_index, content, embedding, token_count, created_at)
            VALUES (:id, :docId, :tenantId, :idx, :content, CAST(:embedding AS vector), :tokenCount, NOW())
            """;

        entityManager.createNativeQuery(sql)
                .setParameter("id", chunkId)
                .setParameter("docId", doc.getId())
                .setParameter("tenantId", tenant.getId())
                .setParameter("idx", index)
                .setParameter("content", content)
                .setParameter("embedding", vectorStr)
                .setParameter("tokenCount", tokenCount)
                .executeUpdate();
    }

    private void createAcl(Document doc, Tenant tenant, String subjectType, String subjectId) {
        AclEntry acl = AclEntry.builder()
                .document(doc)
                .tenant(tenant)
                .subjectType(subjectType)
                .subjectId(subjectId)
                .permission("READ")
                .build();
        aclEntryRepository.save(acl);
    }

    private String formatVector(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    private String hashKey(String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not supported", e);
        }
    }
}
