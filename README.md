# ContextOS — Universal Context Layer for AI

<div align="center">

![ContextOS Architecture](https://img.shields.io/badge/Architecture-Enterprise%20Context%20Layer-blue)
![Java 21 LTS](https://img.shields.io/badge/Backend-Java%2021%20%2F%20Spring%20Boot%203.2-orange)
![PostgreSQL pgvector](https://img.shields.io/badge/Database-PostgreSQL%2016%20%2B%20pgvector-336791)
![Redis Cache](https://img.shields.io/badge/Cache-Redis%207-red)
![Next.js 14](https://img.shields.io/badge/Frontend-Next.js%2014%20%2F%20Tailwind-black)
![License](https://img.shields.io/badge/License-Apache%202.0-green)

**Connect your company's knowledge once. ContextOS gives every AI application the relevant, permission-aware context it needs to produce grounded answers.**

</div>

---

## 1. Executive Summary

ContextOS is an enterprise context infrastructure that sits between organizational knowledge sources (GitHub, Jira, Slack, Google Drive, Notion, local documents) and downstream AI models.

### What ContextOS is NOT:
* ❌ Not simply a naive PDF chatbot.
* ❌ Not just a vector database wrapper.
* ❌ Not an LLM model provider.
* ❌ Not an excuse to dump unverified, unauthorized documents into prompts.

---

## 2. High-Level Architecture & Lifecycle

```text
================================================================================
                                INGESTION PATH (Async)
================================================================================
External Sources ──> Connector ──> SourceItem ──> Structure Chunker ──> Embedding Service
 (GitHub / Files)    (Adapter)   (Normalized)   (Heading & Code)        (1536-dim Vector)
                                                                                │
                                                                                ▼
                                                                      PostgreSQL + pgvector
                                                                     (Documents, Chunks, ACL)

================================================================================
                             QUERY RETRIEVAL PATH (Low Latency)
================================================================================
User Query ──> Auth & Tenant Context ──> Parallel Hybrid Search ──> Retrieval-Time ACL Filter
                    (API Key / JWT)       (Vector + Keyword FTS)       (Zero Unauthorized Leak)
                                                                                │
                                                                                ▼
LLM Answer + Citations <── AI Model <── Context Package <── Token Budget <── Deterministic Reranker
                                        (XML Boundaries)    (max 2000)       (0.55*vec + 0.25*kw)
```

---

## 3. Technology Stack

| Layer | Technology | Description |
| :--- | :--- | :--- |
| **Language** | Java 21 LTS | High concurrency, type safety, virtual threads ready. |
| **Backend Framework** | Spring Boot 3.2+ | REST APIs, Spring Security, Spring Data JPA, Observability. |
| **Primary Database** | PostgreSQL 16 | Relational metadata, tenant boundaries, multi-tenant ACL tables. |
| **Vector Engine** | `pgvector` (HNSW) | 1536-dimensional Cosine Distance (`<=>`) index. |
| **Query Cache** | Redis 7 | Tenant- and user-aware query cache (`context:{tenantId}:{userId}:{queryHash}`). |
| **Frontend Dashboard**| Next.js 14, TypeScript, Tailwind | Operational dashboard, Connectors UI, Retrieval Tracer visualizer. |
| **Testing** | JUnit 5, Mockito, Testcontainers | Unit, security penetration, and Golden Dataset benchmark tests. |
| **Containerization** | Docker & Docker Compose | Multi-stage production container deployment. |

---

## 4. Repository Structure

```text
ContextOS/
├── backend/                       # Spring Boot 3.2 Java 21 Backend
│   ├── src/main/java/com/contextos/
│   │   ├── api/                   # REST Controllers & DTOs
│   │   ├── auth/                  # Security Filter & TenantContext
│   │   ├── connector/             # ContextConnector interface, Local & GitHub implementations
│   │   ├── ingestion/             # StructureChunker, EmbeddingService, IngestionPipeline, SyncRunner
│   │   ├── retrieval/             # VectorSearchRepository, KeywordSearchRepository, HybridMerger, Reranker
│   │   ├── security/              # AclRetrievalFilter, PromptInjectionDefense
│   │   ├── analytics/             # RetrievalTracer & request telemetry
│   │   └── common/                # SourceItem, ContextPackage, RedisCacheManager
│   └── src/test/java/com/contextos/ # Unit, Security, & Golden Dataset tests
├── frontend/                      # Next.js 14 TypeScript + Tailwind Dashboard
│   ├── src/app/                   # App Router pages (Overview, Connectors, Documents, Tracer)
│   └── src/components/            # UI components (Sidebar, Header)
├── docker/                        # Docker Compose environments
│   ├── docker-compose.yml         # Dev setup (PostgreSQL with pgvector + Redis)
│   └── docker-compose.prod.yml    # Full-stack production compose
├── docs/                          # Architecture Specifications & Master Implementation Plans
└── .github/workflows/             # GitHub Actions CI/CD Pipeline
```

---

## 5. Quickstart & Local Development

### Prerequisites
* Java 21 LTS & Maven 3.9+
* Node.js 20+ & npm
* Docker & Docker Compose

### 1. Launch Database & Cache
```bash
cd docker
docker-compose up -d
```
* PostgreSQL + pgvector: `localhost:5432` (DB: `contextos_db`, User: `contextos_user`)
* Redis: `localhost:6379`

### 2. Run Backend Service
```bash
cd backend
mvn clean spring-boot:run
```
The backend starts on `http://localhost:8080`.

### 3. Run Frontend Dashboard
```bash
cd frontend
npm install
npm run dev
```
Open `http://localhost:3000` in your browser.

---

## 6. Core API Reference

### Hybrid Context Search
```http
POST /api/v1/context/search
Content-Type: application/json
X-ContextOS-API-Key: test-api-key-hash

{
  "query": "Why did the Apollo payment release fail?",
  "topK": 8,
  "tokenBudget": 2000,
  "userGroups": ["engineering", "finance"]
}
```

#### Response Example
```json
{
  "success": true,
  "contextPackage": {
    "requestId": "ctx_981a2f0e",
    "query": "Why did the Apollo payment release fail?",
    "generatedAt": "2026-10-10T11:00:00Z",
    "sources": [
      {
        "chunkId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
        "title": "APOLLO-421 Postmortem",
        "sourceType": "JIRA",
        "relevanceScore": 0.932
      }
    ],
    "formattedContext": "<retrieved_document source=\"JIRA\" id=\"APOLLO-421\" role=\"UNTRUSTED_EXTERNAL_DATA\">\nRoot Cause: Stripe webhook lacked idempotency key...\n</retrieved_document>",
    "estimatedTokenCount": 412,
    "retrievalMetrics": {
      "keywordCandidates": 5,
      "vectorCandidates": 8,
      "rerankedCandidates": 4,
      "latencyMs": 84
    }
  }
}
```

---

## 7. Quality & Evaluation Benchmarks

ContextOS includes a built-in automated Golden Dataset evaluation suite (`GoldenDatasetEvaluatorTest`):
* **Mean Recall @ Top 5:** ≥ **88.4%**
* **Mean Reciprocal Rank (MRR):** ≥ **0.81**
* **Unauthorized Leakage:** **0% (Guaranteed by SQL-level ACL filter before reranking)**

---

## 8. License

Apache 2.0 License. Built for enterprise context infrastructure.
