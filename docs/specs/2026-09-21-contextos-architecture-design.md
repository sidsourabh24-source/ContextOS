# ContextOS — Architecture & System Design Document

**Product:** ContextOS (Universal Context Layer for AI)  
**Version:** 1.0.0  
**Date:** September 21, 2026  
**Status:** Approved Architectural Specification  

---

## 1. Executive Summary & Product Vision

ContextOS is an enterprise-grade context infrastructure for AI applications. It bridges scattered organizational knowledge (GitHub, Jira, Slack, Google Drive, Notion, local documentation) with AI LLM applications. 

ContextOS ingests and indexes enterprise knowledge, applies strict multi-tenant access control list (ACL) permissions, performs hybrid keyword and semantic vector retrieval, reranks results, and returns token-budgeted, source-attributed context packages for AI models.

### Key Non-Goals
* ContextOS is **not** just a PDF chatbot.
* ContextOS is **not** merely a vector databasewrapper.
* ContextOS is **not** an LLM model provider.
* ContextOS does **not** bypass enterprise source system authorizations.

---

## 2. Technology Stack & Architectural Decisions

| System Layer | Technology Selected | Rationale |
| :--- | :--- | :--- |
| **Backend Runtime** | Java 21 LTS | Enterprise performance, strong type safety, concurrency features |
| **Backend Framework** | Spring Boot 3.x | Production REST APIs, Dependency Injection, Spring Security |
| **Database & Vector** | PostgreSQL 16 + `pgvector` | Combined relational metadata, multi-tenant ACLs, FTS, and 1536d vector search |
| **Cache & Rate Limiting** | Redis 7.x | Low-latency query caching, connector health tracking, API rate limits |
| **Messaging (V2)** | Apache Kafka | Asynchronous pipeline decoupling for high-volume document ingestion |
| **Frontend UI** | Next.js 14 + TypeScript + Tailwind CSS | Admin dashboard, connector management, retrieval visualizer & tracing |
| **Testing** | JUnit 5, Mockito, Testcontainers | Reliable unit tests, security assertion tests, containerized integration tests |
| **DevOps / CI/CD** | Docker Compose, GitHub Actions, AWS | Repeatable containerized environments and enterprise deployment |

---

## 3. High-Level Architecture & Data Flow

```text
================================================================================
                                INGESTION PATH (Async)
================================================================================
External Source  ──>  Connector  ──>  SourceItem  ──>  Parser & Chunker  ──>  Embedding Service
 (GitHub/Files)        (Adapter)      (Normalized)        (Structure-aware)       (Vector)
                                                                                  │
                                                                                  ▼
                                                                        PostgreSQL + pgvector
                                                                       (Docs, Chunks, ACLs)

================================================================================
                                 QUERY PATH (Low Latency)
================================================================================
User Query  ──>  Auth / Tenant Check  ──>  Hybrid Search  ──>  ACL Retrieval Filter  ──>  Reranker  ──>  Context Package
  (API)           (JWT/API Key)           (Vector + FTS)        (Enforced BEFORE LLM)     (Weighted)      (With Citations)
```

---

## 4. Package & Component Structure

The Java backend code will follow a clean, modular architecture:

```text
d:\ContentOS\
├── backend/
│   ├── src/main/java/com/contextos/
│   │   ├── auth/              # Identity, JWT, API Key hashing, TenantContext
│   │   ├── tenant/            # Tenant entity, Organization context, Isolation filters
│   │   ├── api/               # REST Controllers, DTOs, Exception handling
│   │   ├── connector/         # ContextConnector interface + GitHub & Local implementations
│   │   ├── ingestion/         # Text Parsers, Structure Chunker, Embeddings client, Sync scheduler
│   │   ├── retrieval/         # Keyword FTS, pgvector search, Hybrid merger, Weighted Reranker
│   │   ├── security/          # ACL Filter predicates, RBAC checkers, Prompt Injection Defense
│   │   ├── analytics/         # Telemetry metrics (Recall@K, MRR, latency tracer)
│   │   └── common/            # SourceItem, ContextPackage, common utility classes
│   └── src/test/java/com/contextos/
├── frontend/                  # Next.js TypeScript Dashboard
├── docker/                    # Docker Compose configs
└── docs/                      # Architectural specs & API contracts
```

---

## 5. Database Schema & Data Models

PostgreSQL schema containing 9 core tables:

1. **`tenants`**: `id` (UUID), `name`, `plan`, `created_at`
2. **`users`**: `id` (UUID), `tenant_id` (FK), `email`, `role` (ADMIN/DEVELOPER/USER), `status`
3. **`api_keys`**: `id` (UUID), `tenant_id` (FK), `key_hash`, `name`, `status`, `created_at`
4. **`connectors`**: `id` (UUID), `tenant_id` (FK), `type` (GITHUB/LOCAL), `name`, `status`, `config_json`, `last_sync_at`
5. **`documents`**: `id` (UUID), `tenant_id` (FK), `connector_id` (FK), `source_id`, `title`, `url`, `checksum`, `updated_at`
6. **`chunks`**: `id` (UUID), `document_id` (FK), `chunk_index`, `content` (TEXT), `embedding` (`vector(1536)`), `token_count`
7. **`acl_entries`**: `id` (UUID), `chunk_id` (FK), `subject_type` (USER/GROUP), `subject_id`, `permission` (READ)
8. **`sync_runs`**: `id` (UUID), `connector_id` (FK), `started_at`, `completed_at`, `status`, `items_processed`, `error_log`
9. **`retrieval_requests`**: `id` (UUID), `tenant_id` (FK), `query`, `latency_ms`, `top_k`, `created_at`

---

## 6. Security, ACL & Prompt Injection Boundaries

1. **Strict Multi-Tenant Scope:** Every SQL query includes `WHERE tenant_id = :tenantId` resolved from the authenticated JWT/API key.
2. **Retrieval-Time Authorization:** Candidates are filtered by ACL rules in SQL before ranking. Unauthorized chunks never enter application memory.
3. **Prompt Injection Defense:** Retrieved chunks are wrapped in XML/Markdown boundaries distinguishing untrusted data from system instructions:
   ```xml
   <retrieved_document source="GITHUB" id="PR-981" title="Payment Fix">
   ... chunk text content ...
   </retrieved_document>
   ```

---

## 7. Quality & Testing Strategy

1. **Unit Testing:** Unit tests for chunker, score calculation, ACL filters, context assembly.
2. **Integration Testing:** Testcontainers for PostgreSQL + `pgvector` + Redis.
3. **Security Testing:** Cross-tenant leakage tests, unauthorized document access attempts.
4. **Golden Dataset Evaluation:** 30–100 question test dataset measuring Recall@K, Precision@K, and MRR.
