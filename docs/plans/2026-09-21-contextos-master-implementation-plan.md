# ContextOS Master Implementation Plan (Module Branches with Daily Commit Progression)

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build ContextOS — a multi-tenant, enterprise context layer platform for AI applications with hybrid search, pgvector, ACL-aware retrieval, and a Next.js admin dashboard.

**Architecture:** Spring Boot 3.x (Java 21) backend + PostgreSQL 16 (`pgvector`) database + Redis cache + Next.js 14 TypeScript dashboard. Organized into 6 module feature branches with daily incremental commits for active GitHub contribution history.

**Tech Stack:** Java 21, Spring Boot 3.2+, PostgreSQL 16 + pgvector, Redis, Next.js 14, TypeScript, Tailwind CSS, Docker Compose, JUnit 5, Testcontainers.

**Spec:** [docs/specs/2026-09-21-contextos-architecture-design.md](file:///d:/ContentOS/docs/specs/2026-09-21-contextos-architecture-design.md)

---

## Git Branch & Daily Commit Strategy

We will use **6 Module Feature Branches**, where each day's task is completed with a **dedicated daily commit** to maintain consistent GitHub contributions:

1. **`main`** — Production-ready, stable codebase.
2. **`feature/module-1-days-01-05`** — Foundation, Scaffolding, DB Schema, JPA Entities, Security & Multi-Tenancy (5 daily commits).
3. **`feature/module-2-days-06-10`** — Connector Interface, Local File & GitHub Connectors, Checksum & Sync Runner (5 daily commits).
4. **`feature/module-3-days-11-15`** — Structure Chunker, Embeddings Adapter, pgvector Search & FTS Search (5 daily commits).
5. **`feature/module-4-days-16-20`** — Hybrid Merger, ACL Filter, Reranker, Context Assembler & REST API (5 daily commits).
6. **`feature/module-5-days-21-25`** — Redis Cache, Telemetry Tracing & Next.js Admin Dashboard UI (5 daily commits).
7. **`feature/module-6-days-26-30`** — Golden Dataset Evaluation, Security Hardening, Docker Compose & CI/CD Pipeline (5 daily commits).

---

## Global Constraints

- Backend Java version: Java 21 LTS (`pom.xml` target 21).
- Repository layout: `backend/` for Java, `frontend/` for Next.js, `docker/` for Docker Compose, `docs/` for specs & plans.
- Database Schema: PostgreSQL 16 with `pgvector` enabled (vector dimension: 1536).
- Security: Multi-tenant `tenantId` resolution mandatory on every query and endpoint.

---

## Review Focus

1. **Cross-Tenant Data Leakage:** Ensure `tenantId` is extracted from security identity and enforced in SQL.
2. **Unauthorized LLM Context Exposure:** Ensure ACL authorization predicate filters candidate chunks in SQL *before* ranking or context assembly.
3. **Stale Vector Embeddings:** Ensure change detection (MD5/SHA256 checksums) prevents re-embedding unchanged documents.
4. **Prompt Injection Boundary Breaches:** Ensure retrieved source chunks are wrapped in XML boundaries (`<retrieved_document>`).
5. **Query Performance:** Ensure pgvector HNSW indexes and composite SQL indexes (`tenant_id`, `created_at`) are created.

---

## Module 1: Foundation & Multi-Tenant Infrastructure
**Branch:** `feature/module-1-days-01-05`

### Task 1.1: Day 01 — Backend Scaffolding & Maven Spring Boot Java 21 Setup
**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/contextos/ContextOSApplication.java`
- Create: `backend/src/main/resources/application.yml`
- Create: `backend/src/test/java/com/contextos/ContextOSApplicationTests.java`

- [ ] **Step 1: Create `backend/pom.xml`** with Spring Boot 3.2 dependencies (Web, Data JPA, Security, Postgres, Redis, Validation, Lombok, Jackson).
- [ ] **Step 2: Create main application class `ContextOSApplication.java`** and `application.yml`.
- [ ] **Step 3: Create base test `ContextOSApplicationTests.java`**.
- [ ] **Step 4: Verify test builds cleanly**.
- [ ] **Step 5: Commit (Day 1 Commit):** `feat(day-01): initialize Spring Boot 3.2 Java 21 backend scaffolding`.

### Task 1.2: Day 02 — Docker Compose Environment (Postgres + pgvector + Redis)
**Files:**
- Create: `docker/docker-compose.yml`
- Create: `docker/postgres/init-pgvector.sql`
- Modify: `backend/src/main/resources/application.yml`

- [ ] **Step 1: Create `docker-compose.yml`** with `pgvector/pgvector:pg16` and `redis:7-alpine`.
- [ ] **Step 2: Create `init-pgvector.sql`** with vector extension initialization.
- [ ] **Step 3: Update `application.yml`** credentials.
- [ ] **Step 4: Verify docker compose syntax**.
- [ ] **Step 5: Commit (Day 2 Commit):** `feat(day-02): add docker compose for postgres pgvector and redis`.

### Task 1.3: Day 03 — Database Schema Migrations (Flyway SQL)
**Files:**
- Create: `backend/src/main/resources/db/migration/V1__initial_schema.sql`

- [ ] **Step 1: Create Flyway migration `V1__initial_schema.sql`** with 9 core tables.
- [ ] **Step 2: Add indexes for `tenant_id` and `vector` search**.
- [ ] **Step 3: Commit (Day 3 Commit):** `feat(day-03): add flyway migration for core multi-tenant schema`.

### Task 1.4: Day 04 — JPA Domain Entities & Repositories
**Files:**
- Create: `backend/src/main/java/com/contextos/tenant/model/Tenant.java`
- Create: `backend/src/main/java/com/contextos/auth/model/User.java`
- Create: `backend/src/main/java/com/contextos/auth/model/ApiKey.java`
- Create: `backend/src/main/java/com/contextos/connector/model/Connector.java`
- Create: `backend/src/main/java/com/contextos/ingestion/model/Document.java`
- Create: `backend/src/main/java/com/contextos/ingestion/model/Chunk.java`
- Create: `backend/src/main/java/com/contextos/security/model/AclEntry.java`

- [ ] **Step 1: Create JPA Entity classes**.
- [ ] **Step 2: Create Spring Data Repositories**.
- [ ] **Step 3: Write Repository unit tests**.
- [ ] **Step 4: Commit (Day 4 Commit):** `feat(day-04): implement JPA entities and repositories`.

### Task 1.5: Day 05 — Spring Security & Multi-Tenant Context
**Files:**
- Create: `backend/src/main/java/com/contextos/auth/TenantContext.java`
- Create: `backend/src/main/java/com/contextos/auth/ApiKeyAuthenticationFilter.java`
- Create: `backend/src/main/java/com/contextos/auth/SecurityConfig.java`

- [ ] **Step 1: Implement `TenantContext` ThreadLocal holder**.
- [ ] **Step 2: Implement `ApiKeyAuthenticationFilter`**.
- [ ] **Step 3: Create Spring `SecurityConfig`**.
- [ ] **Step 4: Write Security Filter test**.
- [ ] **Step 5: Commit (Day 5 Commit):** `feat(day-05): configure multi-tenant security and api key auth filter`.

---

## Module 2: Source Connector Architecture & Ingestion Core
**Branch:** `feature/module-2-days-06-10`

### Task 2.1: Day 06 — Connector Interface & Normalized SourceItem Model
**Files:**
- Create: `backend/src/main/java/com/contextos/common/SourceItem.java`
- Create: `backend/src/main/java/com/contextos/connector/ContextConnector.java`

- [ ] **Step 1: Define `SourceItem` Java record**.
- [ ] **Step 2: Define `ContextConnector` interface**.
- [ ] **Step 3: Write unit tests for `SourceItem`**.
- [ ] **Step 4: Commit (Day 6 Commit):** `feat(day-06): define connector interface and normalized source item`.

### Task 2.2: Day 07 — Local File & Directory Source Connector
**Files:**
- Create: `backend/src/main/java/com/contextos/connector/local/LocalFileConnector.java`

- [ ] **Step 1: Implement `LocalFileConnector`** reading local markdown/text/pdf files into `SourceItem` records.
- [ ] **Step 2: Write connector tests**.
- [ ] **Step 3: Commit (Day 7 Commit):** `feat(day-07): implement local file source connector`.

### Task 2.3: Day 08 — GitHub API Repository Connector
**Files:**
- Create: `backend/src/main/java/com/contextos/connector/github/GitHubConnector.java`

- [ ] **Step 1: Implement `GitHubConnector`** fetching code/issues into `SourceItem` records.
- [ ] **Step 2: Write GitHub connector unit test**.
- [ ] **Step 3: Commit (Day 8 Commit):** `feat(day-08): implement github api repository connector`.

### Task 2.4: Day 09 — Checksum Change Detector & Document Normalizer
**Files:**
- Create: `backend/src/main/java/com/contextos/ingestion/ChecksumCalculator.java`
- Create: `backend/src/main/java/com/contextos/ingestion/DocumentNormalizer.java`

- [ ] **Step 1: Implement SHA-256 checksum calculator**.
- [ ] **Step 2: Implement `DocumentNormalizer`** for change detection.
- [ ] **Step 3: Commit (Day 9 Commit):** `feat(day-09): implement checksum change detection and document normalizer`.

### Task 2.5: Day 10 — Sync Runner & Audit Logger
**Files:**
- Create: `backend/src/main/java/com/contextos/ingestion/SyncRunner.java`

- [ ] **Step 1: Implement `SyncRunner`** for async sync execution and audit logging.
- [ ] **Step 2: Write integration test for `SyncRunner`**.
- [ ] **Step 3: Commit (Day 10 Commit):** `feat(day-10): implement async sync runner and audit logger`.

---

## Module 3: Chunking, Embeddings & Vector Indexing
**Branch:** `feature/module-3-days-11-15`

### Task 3.1: Day 11 — Structure-Aware Document Chunker
**Files:**
- Create: `backend/src/main/java/com/contextos/ingestion/chunker/StructureChunker.java`

- [ ] **Step 1: Implement markdown and code structure-aware chunker**.
- [ ] **Step 2: Write chunker unit tests**.
- [ ] **Step 3: Commit (Day 11 Commit):** `feat(day-11): implement structure aware document chunker`.

### Task 3.2: Day 12 — Embedding Service Adapter
**Files:**
- Create: `backend/src/main/java/com/contextos/ingestion/embedding/EmbeddingService.java`

- [ ] **Step 1: Implement `EmbeddingService`**.
- [ ] **Step 2: Write embedding service tests**.
- [ ] **Step 3: Commit (Day 12 Commit):** `feat(day-12): implement embedding service client adapter`.

### Task 3.3: Day 13 — pgvector Native Repository & Vector Search
**Files:**
- Create: `backend/src/main/java/com/contextos/retrieval/vector/VectorSearchRepository.java`

- [ ] **Step 1: Implement `VectorSearchRepository`** with pgvector `<->` cosine similarity.
- [ ] **Step 2: Write repository tests**.
- [ ] **Step 3: Commit (Day 13 Commit):** `feat(day-13): implement pgvector cosine similarity search repository`.

### Task 3.4: Day 14 — PostgreSQL Full-Text Search (Keyword Search)
**Files:**
- Create: `backend/src/main/java/com/contextos/retrieval/keyword/KeywordSearchRepository.java`

- [ ] **Step 1: Implement `KeywordSearchRepository`** using PostgreSQL `tsvector`/`tsquery`.
- [ ] **Step 2: Write keyword search tests**.
- [ ] **Step 3: Commit (Day 14 Commit):** `feat(day-14): implement postgresql full text search repository`.

### Task 3.5: Day 15 — Ingestion Pipeline Orchestrator
**Files:**
- Create: `backend/src/main/java/com/contextos/ingestion/IngestionPipeline.java`

- [ ] **Step 1: Implement `IngestionPipeline`** orchestrating full ingestion flow.
- [ ] **Step 2: Write end-to-end integration test**.
- [ ] **Step 3: Commit (Day 15 Commit):** `feat(day-15): orchestrate end-to-end ingestion pipeline`.

---

## Module 4: Hybrid Retrieval, Security & Context API
**Branch:** `feature/module-4-days-16-20`

### Task 4.1: Day 16 — Hybrid Search Merger
**Files:**
- Create: `backend/src/main/java/com/contextos/retrieval/hybrid/HybridSearchMerger.java`

- [ ] **Step 1: Implement parallel hybrid search candidate merger**.
- [ ] **Step 2: Commit (Day 16 Commit):** `feat(day-16): implement hybrid search candidate merger`.

### Task 4.2: Day 17 — ACL Retrieval Filter
**Files:**
- Create: `backend/src/main/java/com/contextos/security/acl/AclRetrievalFilter.java`

- [ ] **Step 1: Implement `AclRetrievalFilter`** checking DB permissions before ranking.
- [ ] **Step 2: Write security tests**.
- [ ] **Step 3: Commit (Day 17 Commit):** `feat(day-17): implement retrieval time acl authorization filter`.

### Task 4.3: Day 18 — Deterministic Score Reranker
**Files:**
- Create: `backend/src/main/java/com/contextos/retrieval/reranker/DeterministicReranker.java`

- [ ] **Step 1: Implement weighted score reranker**.
- [ ] **Step 2: Commit (Day 18 Commit):** `feat(day-18): implement deterministic score reranker`.

### Task 4.4: Day 19 — Context Assembler & Token Budget Manager
**Files:**
- Create: `backend/src/main/java/com/contextos/retrieval/context/ContextAssembler.java`

- [ ] **Step 1: Implement token budget context assembler with source citations**.
- [ ] **Step 2: Commit (Day 19 Commit):** `feat(day-19): implement token budget context assembler with citations`.

### Task 4.5: Day 20 — Context Search REST API Endpoint
**Files:**
- Create: `backend/src/main/java/com/contextos/api/controller/ContextSearchController.java`

- [ ] **Step 1: Implement `POST /api/v1/context/search` endpoint**.
- [ ] **Step 2: Write API integration tests**.
- [ ] **Step 3: Commit (Day 20 Commit):** `feat(day-20): implement context search rest api endpoint`.

---

## Module 5: Redis Caching, Observability & Admin Dashboard UI
**Branch:** `feature/module-5-days-21-25`

### Task 5.1: Day 21 — Redis Cache Layer
**Files:**
- Create: `backend/src/main/java/com/contextos/common/cache/RedisCacheManager.java`

- [ ] **Step 1: Implement tenant-aware Redis query caching**.
- [ ] **Step 2: Commit (Day 21 Commit):** `feat(day-21): implement tenant aware redis cache layer`.

### Task 5.2: Day 22 — Retrieval Telemetry Tracing
**Files:**
- Create: `backend/src/main/java/com/contextos/analytics/RetrievalTracer.java`

- [ ] **Step 1: Implement retrieval telemetry tracer**.
- [ ] **Step 2: Commit (Day 22 Commit):** `feat(day-22): implement retrieval telemetry and request tracing`.

### Task 5.3: Day 23 — Next.js 14 Dashboard UI Scaffolding
**Files:**
- Create: `frontend/src/app/layout.tsx`

- [ ] **Step 1: Initialize Next.js 14 TypeScript dashboard layout**.
- [ ] **Step 2: Commit (Day 23 Commit):** `feat(day-23): initialize nextjs 14 dashboard frontend scaffolding`.

### Task 5.4: Day 24 — Connectors UI & Knowledge Explorer Pages
**Files:**
- Create: `frontend/src/app/connectors/page.tsx`
- Create: `frontend/src/app/documents/page.tsx`

- [ ] **Step 1: Build Connectors & Knowledge Explorer UI pages**.
- [ ] **Step 2: Commit (Day 24 Commit):** `feat(day-24): build connectors and knowledge explorer ui pages`.

### Task 5.5: Day 25 — Retrieval Tracer UI & Visualizer Page
**Files:**
- Create: `frontend/src/app/retrieval-tracer/page.tsx`

- [ ] **Step 1: Build Retrieval Visualizer UI page**.
- [ ] **Step 2: Commit (Day 25 Commit):** `feat(day-25): build retrieval tracer visualizer page`.

---

## Module 6: Quality Evaluation, Security Hardening & DevOps
**Branch:** `feature/module-6-days-26-30`

### Task 6.1: Day 26 — Golden Dataset Evaluation Harness
**Files:**
- Create: `backend/src/test/java/com/contextos/eval/GoldenDatasetEvaluatorTest.java`

- [ ] **Step 1: Implement Golden Dataset test runner asserting Recall@K >= 0.80 and MRR >= 0.70**.
- [ ] **Step 2: Commit (Day 26 Commit):** `test(day-26): implement golden dataset retrieval evaluation test harness`.

### Task 6.2: Day 27 — Prompt Injection Defense
**Files:**
- Create: `backend/src/main/java/com/contextos/security/PromptInjectionDefense.java`

- [ ] **Step 1: Implement prompt injection boundary defense**.
- [ ] **Step 2: Commit (Day 27 Commit):** `feat(day-27): implement prompt injection boundary defense`.

### Task 6.3: Day 28 — Security Penetration & Revocation Suite
**Files:**
- Create: `backend/src/test/java/com/contextos/security/CrossTenantSecurityTest.java`

- [ ] **Step 1: Implement cross-tenant security penetration tests**.
- [ ] **Step 2: Commit (Day 28 Commit):** `test(day-28): add cross tenant leakage and permission revocation security tests`.

### Task 6.4: Day 29 — Production Multi-Stage Dockerfiles
**Files:**
- Create: `backend/Dockerfile`
- Create: `frontend/Dockerfile`

- [ ] **Step 1: Create production multi-stage Dockerfiles**.
- [ ] **Step 2: Commit (Day 29 Commit):** `build(day-29): create production dockerfiles for backend and frontend`.

### Task 6.5: Day 30 — CI/CD Pipeline & Final Project Documentation
**Files:**
- Create: `.github/workflows/ci.yml`
- Create: `README.md`

- [ ] **Step 1: Create GitHub Actions CI workflow**.
- [ ] **Step 2: Create comprehensive `README.md`**.
- [ ] **Step 3: Commit (Day 30 Commit):** `ci(day-30): add github actions workflow and project readme`.
