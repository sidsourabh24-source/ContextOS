"use client";

import React, { useState } from "react";
import { Search, Zap, Shield, Filter, FileText, CheckCircle2, XCircle, ArrowRight, Sparkles } from "lucide-react";

export default function RetrievalTracerPage() {
  const [query, setQuery] = useState("Why did APOLLO-421 payment release fail?");
  const [isRunning, setIsRunning] = useState(false);

  const handleRunTrace = (e: React.FormEvent) => {
    e.preventDefault();
    setIsRunning(true);
    setTimeout(() => {
      setIsRunning(false);
    }, 600);
  };

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      <div>
        <h2 className="text-2xl font-bold tracking-tight text-white">Retrieval Tracer & Explainability</h2>
        <p className="text-sm text-slate-400 mt-1">
          Inspect every retrieval stage: Vector & Keyword candidates, ACL permission stripping, reranking, and final assembled LLM context.
        </p>
      </div>

      {/* Query Bar */}
      <form onSubmit={handleRunTrace} className="glass-panel p-2.5 rounded-2xl flex items-center gap-3 border border-slate-800">
        <Search className="h-5 w-5 text-slate-400 ml-3" />
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Ask a query to trace retrieval..."
          className="flex-1 bg-transparent border-none text-sm text-white placeholder-slate-500 focus:outline-none"
        />
        <button
          type="submit"
          disabled={isRunning}
          className="flex items-center gap-2 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-500 hover:to-indigo-500 text-white text-xs font-semibold px-5 py-2.5 rounded-xl shadow-lg shadow-blue-500/20 transition"
        >
          <Sparkles className="h-4 w-4" />
          {isRunning ? "Tracing..." : "Trace Retrieval"}
        </button>
      </form>

      {/* Multi-Stage Visualizer Pipeline */}
      <div className="grid grid-cols-1 lg:grid-cols-4 gap-5">
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
              <div className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-xs">
                <p className="font-semibold text-slate-200">APOLLO-421 Postmortem</p>
                <span className="text-[10px] text-purple-400 font-mono">Similarity: 0.94</span>
              </div>
              <div className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-xs">
                <p className="font-semibold text-slate-200">PR #981: Payment Webhook</p>
                <span className="text-[10px] text-purple-400 font-mono">Similarity: 0.88</span>
              </div>
            </div>
          </div>
          <span className="text-[11px] text-slate-500 mt-4 block">8 candidates retrieved</span>
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
                tsvector / ts_rank
              </span>
            </div>
            <p className="text-xs text-slate-400 mb-3">Exact ID & ticket matching</p>
            <div className="space-y-2">
              <div className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-xs">
                <p className="font-semibold text-slate-200">APOLLO-421 JIRA Ticket</p>
                <span className="text-[10px] text-blue-400 font-mono">Rank: 0.98</span>
              </div>
              <div className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-xs">
                <p className="font-semibold text-slate-200">Incident Channel Discussion</p>
                <span className="text-[10px] text-blue-400 font-mono">Rank: 0.72</span>
              </div>
            </div>
          </div>
          <span className="text-[11px] text-slate-500 mt-4 block">5 candidates retrieved</span>
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
            <p className="text-xs text-slate-400 mb-3">Applied before LLM context</p>
            <div className="space-y-2">
              <div className="p-2.5 rounded-xl bg-slate-900 border border-emerald-500/30 text-xs flex items-center justify-between">
                <div>
                  <p className="font-semibold text-slate-200">APOLLO-421</p>
                  <span className="text-[10px] text-emerald-400">Allowed (TEAM)</span>
                </div>
                <CheckCircle2 className="h-4 w-4 text-emerald-400 shrink-0" />
              </div>
              <div className="p-2.5 rounded-xl bg-slate-900 border border-rose-500/30 text-xs flex items-center justify-between">
                <div>
                  <p className="font-semibold text-slate-400">Finance Settlement Sheet</p>
                  <span className="text-[10px] text-rose-400">Blocked (PRIVATE)</span>
                </div>
                <XCircle className="h-4 w-4 text-rose-400 shrink-0" />
              </div>
            </div>
          </div>
          <span className="text-[11px] text-slate-500 mt-4 block">1 unauthorized item excluded</span>
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
              <div className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-xs">
                <p className="font-semibold text-white">1. APOLLO-421</p>
                <span className="text-[10px] text-amber-400 font-mono">Score: 0.932</span>
              </div>
              <div className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-xs">
                <p className="font-semibold text-white">2. PR #981 Payment Fix</p>
                <span className="text-[10px] text-amber-400 font-mono">Score: 0.865</span>
              </div>
            </div>
          </div>
          <span className="text-[11px] text-slate-500 mt-4 block">Selected Top 4 chunks</span>
        </div>
      </div>

      {/* Final Assembled Context Package Output */}
      <div className="glass-panel p-6 rounded-2xl border border-slate-800">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <FileText className="h-5 w-5 text-blue-400" />
            <h3 className="text-base font-semibold text-white">Final Assembled LLM Evidence Context</h3>
          </div>
          <span className="text-xs font-mono text-slate-400 bg-slate-900 px-3 py-1 rounded-lg border border-slate-800">
            Token Budget: 412 / 2000 tokens
          </span>
        </div>

        <pre className="p-4 rounded-xl bg-slate-950 border border-slate-800/80 text-xs font-mono text-emerald-400 overflow-x-auto leading-relaxed">
{`<retrieved_document source="JIRA" id="APOLLO-421" title="APOLLO-421: Payment Gateway Timeout">
Root Cause: The payment release failed because the stripe webhook payload lacked idempotency keys,
causing repeated timeout retry storms during peak traffic at 14:00 UTC.
</retrieved_document>

<retrieved_document source="GITHUB" id="PR-981" title="PR #981: Payment Webhook Idempotency">
Fix: Added Redis distributed lock and unique idempotency header verification in StripeWebhookHandler.java.
</retrieved_document>`}
        </pre>
      </div>
    </div>
  );
}
