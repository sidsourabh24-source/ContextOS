"use client";

import React, { useState, useEffect } from "react";
import { Search, Zap, Shield, Filter, FileText, CheckCircle2, XCircle, Sparkles, RefreshCw, AlertCircle } from "lucide-react";

interface SearchResponse {
  success: boolean;
  contextPackage?: {
    requestId: string;
    query: string;
    generatedAt: string;
    sources: Array<{
      chunkId: string;
      documentId: string;
      title: string;
      url: string;
      sourceType: string;
      excerpt: string;
      relevanceScore: number;
      updatedAt: string;
    }>;
    formattedContext: string;
    estimatedTokenCount: number;
    retrievalMetrics?: {
      keywordCandidates: number;
      vectorCandidates: number;
      rerankedCandidates: number;
      latencyMs: number;
    };
  };
  traceDetails?: {
    totalCandidatesCount: number;
    aclAuthorizedCount: number;
    aclFilteredCount: number;
    fromCache: boolean;
    strippedTitles: string[];
  };
  error?: string;
}

export default function RetrievalTracerPage() {
  const [query, setQuery] = useState("Why did APOLLO-421 payment release fail?");
  const [userRole, setUserRole] = useState("ROLE_ENGINEER");
  const [isLoading, setIsLoading] = useState(false);
  const [result, setResult] = useState<SearchResponse | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const executeSearch = async (searchQuery: string, role: string) => {
    setIsLoading(true);
    setErrorMsg(null);

    try {
      const res = await fetch("http://localhost:8080/api/v1/context/search", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-ContextOS-API-Key": "ctx-live-test-key-12345"
        },
        body: JSON.stringify({
          query: searchQuery.trim(),
          topK: 6,
          tokenBudget: 2000,
          userGroups: role === "ROLE_EXECUTIVE" ? ["ROLE_EXECUTIVE", "ROLE_ENGINEER"] : [role]
        })
      });

      if (!res.ok) {
        throw new Error(`Server returned status ${res.status}: ${res.statusText}`);
      }

      const data: SearchResponse = await res.json();
      setResult(data);
    } catch (err: any) {
      console.warn("Backend fetch failed:", err);
      setErrorMsg("Backend initializing or connection error. Ensure 'mvnw.cmd spring-boot:run' is running on port 8080.");
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    executeSearch(query, userRole);
  }, []);

  const handleRunTrace = (e: React.FormEvent) => {
    e.preventDefault();
    executeSearch(query, userRole);
  };

  const handleRoleChange = (newRole: string) => {
    setUserRole(newRole);
    executeSearch(query, newRole);
  };

  const sources = result?.contextPackage?.sources || [];
  const latency = result?.contextPackage?.retrievalMetrics?.latencyMs ?? 138;
  const isCached = result?.traceDetails?.fromCache ?? false;
  const strippedTitles = result?.traceDetails?.strippedTitles || [];

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      <div>
        <div className="flex items-center justify-between flex-wrap gap-4">
          <div>
            <h2 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
              Retrieval Tracer & Explainability
              <span className="text-xs px-2.5 py-0.5 rounded-full font-mono bg-blue-500/10 text-blue-400 border border-blue-500/20">
                Live Engine
              </span>
            </h2>
            <p className="text-sm text-slate-400 mt-1">
              Inspect candidate generation, retrieval-time ACL authorization stripping, reranking, and assembled LLM context.
            </p>
          </div>

          {/* User Role Selector to demonstrate ACL */}
          <div className="flex items-center gap-2 bg-slate-900/80 p-1.5 rounded-xl border border-slate-800">
            <span className="text-xs text-slate-400 px-2 font-medium">Test Persona:</span>
            <button
              type="button"
              onClick={() => handleRoleChange("ROLE_ENGINEER")}
              className={`text-xs px-3 py-1.5 rounded-lg font-medium transition ${
                userRole === "ROLE_ENGINEER"
                  ? "bg-blue-600 text-white shadow"
                  : "text-slate-400 hover:text-slate-200"
              }`}
            >
              Engineer (ROLE_ENGINEER)
            </button>
            <button
              type="button"
              onClick={() => handleRoleChange("ROLE_EXECUTIVE")}
              className={`text-xs px-3 py-1.5 rounded-lg font-medium transition ${
                userRole === "ROLE_EXECUTIVE"
                  ? "bg-purple-600 text-white shadow"
                  : "text-slate-400 hover:text-slate-200"
              }`}
            >
              Executive (ROLE_EXECUTIVE)
            </button>
          </div>
        </div>
      </div>

      {errorMsg && (
        <div className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/20 text-amber-300 text-xs flex items-center gap-2">
          <AlertCircle className="h-4 w-4 shrink-0" />
          <span>{errorMsg}</span>
        </div>
      )}

      {/* Query Bar */}
      <form onSubmit={handleRunTrace} className="glass-panel p-2.5 rounded-2xl flex items-center gap-3 border border-slate-800">
        <Search className="h-5 w-5 text-slate-400 ml-3" />
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Ask a query to trace retrieval (e.g. 'Why did payment release fail?' or 'executive compensation')..."
          className="flex-1 bg-transparent border-none text-sm text-white placeholder-slate-500 focus:outline-none"
        />
        <button
          type="submit"
          disabled={isLoading}
          className="flex items-center gap-2 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white text-xs font-semibold px-5 py-2.5 rounded-xl shadow-lg shadow-blue-500/20 transition disabled:opacity-50"
        >
          {isLoading ? (
            <RefreshCw className="h-4 w-4 animate-spin" />
          ) : (
            <Sparkles className="h-4 w-4" />
          )}
          {isLoading ? "Executing Search..." : "Trace Retrieval"}
        </button>
      </form>

      {/* Latency & Cache Status Banner */}
      <div className="flex items-center justify-between text-xs px-1 text-slate-400 flex-wrap gap-2">
        <div className="flex items-center gap-3">
          <span>Latency: <strong className="text-white font-mono">{latency} ms</strong></span>
          <span>•</span>
          <span>Estimated Tokens: <strong className="text-white font-mono">{result?.contextPackage?.estimatedTokenCount ?? 412} / 2000</strong> tokens</span>
        </div>
        <div className="flex items-center gap-2">
          {isCached ? (
            <span className="bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 px-2.5 py-0.5 rounded-full font-mono text-[11px]">
              ⚡ Redis Cache Hit
            </span>
          ) : (
            <span className="bg-blue-500/10 text-blue-400 border border-blue-500/20 px-2.5 py-0.5 rounded-full font-mono text-[11px]">
              Live Engine Query
            </span>
          )}
        </div>
      </div>

      {/* Multi-Stage Visualizer Pipeline */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
        {/* Stage 1: Vector Search */}
        <div className="glass-panel p-4 rounded-2xl flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs font-bold text-white flex items-center gap-1.5">
                <Zap className="h-4 w-4 text-purple-400" />
                1. Vector Search
              </span>
              <span className="text-[10px] font-mono bg-purple-500/10 text-purple-400 border border-purple-500/20 px-2 py-0.5 rounded-full">
                pgvector (1536d)
              </span>
            </div>
            <p className="text-xs text-slate-400 mb-3">Cosine distance metric (&lt;=&gt;)</p>
            <div className="space-y-2">
              {sources.length > 0 ? (
                sources.slice(0, 2).map((s, i) => (
                  <div key={i} className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-xs">
                    <p className="font-semibold text-slate-200 truncate">{s.title}</p>
                    <span className="text-[10px] text-purple-400 font-mono">Similarity: {s.relevanceScore.toFixed(3)}</span>
                  </div>
                ))
              ) : (
                <div className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-xs text-slate-400">
                  APOLLO-421 Postmortem
                </div>
              )}
            </div>
          </div>
          <span className="text-[11px] text-slate-500 mt-4 block">
            {result?.traceDetails?.totalCandidatesCount ?? sources.length} candidates indexed
          </span>
        </div>

        {/* Stage 2: Keyword FTS */}
        <div className="glass-panel p-4 rounded-2xl flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs font-bold text-white flex items-center gap-1.5">
                <Search className="h-4 w-4 text-blue-400" />
                2. Keyword FTS
              </span>
              <span className="text-[10px] font-mono bg-blue-500/10 text-blue-400 border border-blue-500/20 px-2 py-0.5 rounded-full">
                plainto_tsquery
              </span>
            </div>
            <p className="text-xs text-slate-400 mb-3">Exact keyword / code token match</p>
            <div className="space-y-2">
              {sources.length > 0 ? (
                sources.slice(0, 2).map((s, i) => (
                  <div key={i} className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-xs">
                    <p className="font-semibold text-slate-200 truncate">{s.title}</p>
                    <span className="text-[10px] text-blue-400 font-mono">Source: {s.sourceType}</span>
                  </div>
                ))
              ) : (
                <div className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-xs text-slate-400">
                  Incident Postmortem FTS
                </div>
              )}
            </div>
          </div>
          <span className="text-[11px] text-slate-500 mt-4 block">FTS rank applied</span>
        </div>

        {/* Stage 3: ACL Security Filter */}
        <div className="glass-panel p-4 rounded-2xl flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs font-bold text-white flex items-center gap-1.5">
                <Shield className="h-4 w-4 text-emerald-400" />
                3. ACL Filter
              </span>
              <span className="text-[10px] font-mono bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 px-2 py-0.5 rounded-full">
                Zero Leakage
              </span>
            </div>
            <p className="text-xs text-slate-400 mb-3">Filtered before LLM prompt assembly</p>
            <div className="space-y-2">
              {sources.map((s, i) => (
                <div key={i} className="p-2 rounded-xl bg-slate-900 border border-emerald-500/30 text-xs flex items-center justify-between">
                  <div className="truncate pr-2">
                    <p className="font-semibold text-slate-200 truncate">{s.title}</p>
                    <span className="text-[10px] text-emerald-400">Authorized ({userRole})</span>
                  </div>
                  <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0" />
                </div>
              ))}
              {strippedTitles.length > 0 && strippedTitles.map((title, i) => (
                <div key={`stripped-${i}`} className="p-2 rounded-xl bg-slate-900 border border-rose-500/30 text-xs flex items-center justify-between">
                  <div className="truncate pr-2">
                    <p className="font-semibold text-slate-400 truncate">{title}</p>
                    <span className="text-[10px] text-rose-400">
                      {userRole === "ROLE_ENGINEER" ? "Blocked (Requires ROLE_EXECUTIVE)" : "Restricted by ACL Policy"}
                    </span>
                  </div>
                  <XCircle className="h-4 w-4 text-rose-400 shrink-0" />
                </div>
              ))}
            </div>
          </div>
          <span className="text-[11px] text-slate-500 mt-4 block">
            {strippedTitles.length} restricted item(s) stripped
          </span>
        </div>

        {/* Stage 4: Weighted Reranking */}
        <div className="glass-panel p-4 rounded-2xl flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs font-bold text-white flex items-center gap-1.5">
                <Filter className="h-4 w-4 text-amber-400" />
                4. Reranker
              </span>
              <span className="text-[10px] font-mono bg-amber-500/10 text-amber-400 border border-amber-500/20 px-2 py-0.5 rounded-full">
                Deterministic
              </span>
            </div>
            <p className="text-xs text-slate-400 mb-3">0.55*vec + 0.25*kw + 0.1*fresh</p>
            <div className="space-y-2">
              {sources.map((s, i) => (
                <div key={i} className="p-2 rounded-xl bg-slate-900 border border-slate-800 text-xs">
                  <p className="font-semibold text-white truncate">#{i + 1}. {s.title}</p>
                  <span className="text-[10px] text-amber-400 font-mono">Score: {s.relevanceScore.toFixed(3)}</span>
                </div>
              ))}
            </div>
          </div>
          <span className="text-[11px] text-slate-500 mt-4 block">
            {sources.length} ranked candidate(s)
          </span>
        </div>
      </div>

      {/* Final Assembled Context Package Output */}
      <div className="glass-panel p-6 rounded-2xl border border-slate-800">
        <div className="flex items-center justify-between mb-4 flex-wrap gap-2">
          <div className="flex items-center gap-2">
            <FileText className="h-5 w-5 text-blue-400" />
            <h3 className="text-base font-semibold text-white">Final Assembled LLM Evidence Context (XML Boundaries)</h3>
          </div>
          <span className="text-xs font-mono text-slate-400 bg-slate-900 px-3 py-1 rounded-lg border border-slate-800">
            Estimated Tokens: {result?.contextPackage?.estimatedTokenCount ?? 412} tokens
          </span>
        </div>

        <pre className="p-4 rounded-xl bg-slate-950 border border-slate-800/80 text-xs font-mono text-emerald-400 overflow-x-auto leading-relaxed max-h-96 whitespace-pre-wrap">
{result?.contextPackage?.formattedContext || `<retrieved_context query="Why did APOLLO-421 payment release fail?" generated_at="2026-10-14T21:55:00Z">
  <document id="doc-apollo-421" title="APOLLO-421 Postmortem: Payment Gateway Webhook Timeout Incident" source="GITHUB" relevance="0.940">
    Incident APOLLO-421: On 2026-03-14, the Stripe payment release webhook experienced a 45-second timeout spike. The root cause was thread pool exhaustion in the billing-service due to synchronous external HTTP calls without backoff. Resolution involved migrating to async event-driven queues with dead letter retry.
  </document>
</retrieved_context>`}
        </pre>
      </div>
    </div>
  );
}
