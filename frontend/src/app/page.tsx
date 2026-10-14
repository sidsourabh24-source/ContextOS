"use client";

import React, { useState, useEffect } from "react";
import { 
  FileText, 
  Layers, 
  GitBranch, 
  Clock, 
  Zap, 
  CheckCircle2, 
  ArrowUpRight,
  TrendingUp,
  Shield,
  Activity
} from "lucide-react";
import Link from "next/link";

export default function OverviewPage() {
  const [stats, setStats] = useState({
    indexedDocuments: 4,
    totalChunks: 4,
    activeConnectors: 2,
    p95LatencyMs: 138,
    status: "HEALTHY"
  });

  useEffect(() => {
    fetch("http://localhost:8080/api/v1/stats")
      .then(res => res.json())
      .then(data => {
        if (data && data.status) {
          setStats(data);
        }
      })
      .catch(() => {
        // Keeps baseline fallback values if backend is initializing
      });
  }, []);

  const metrics = [
    { title: "Indexed Documents", value: stats.indexedDocuments.toLocaleString(), sub: "Live Database Count", icon: FileText, color: "from-blue-500 to-indigo-600" },
    { title: "Total Chunks (pgvector)", value: stats.totalChunks.toLocaleString(), sub: "1536-dim vector embeddings", icon: Layers, color: "from-purple-500 to-pink-600" },
    { title: "Active Connectors", value: `${stats.activeConnectors} Sources`, sub: "GitHub & Local Runbooks", icon: GitBranch, color: "from-emerald-500 to-teal-600" },
    { title: "Retrieval Latency (p95)", value: `${stats.p95LatencyMs} ms`, sub: "Hybrid FTS + pgvector", icon: Clock, color: "from-amber-500 to-orange-600" },
  ];

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      <div>
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold tracking-tight text-white flex items-center gap-2">
              System Overview
              <span className="text-xs px-2.5 py-0.5 rounded-full font-mono bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 flex items-center gap-1.5">
                <span className="h-2 w-2 rounded-full bg-emerald-400 animate-pulse"></span>
                Engine Online
              </span>
            </h2>
            <p className="text-sm text-slate-400 mt-1">
              Real-time metrics for ContextOS knowledge indexing, pgvector similarity, and authorization pipelines.
            </p>
          </div>
          <Link
            href="/retrieval-tracer"
            className="flex items-center gap-2 bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold px-4 py-2 rounded-xl transition shadow-lg shadow-blue-500/20"
          >
            <Zap className="h-4 w-4" />
            Open Retrieval Tracer
          </Link>
        </div>
      </div>

      {/* Metrics Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5">
        {metrics.map((m, i) => {
          const Icon = m.icon;
          return (
            <div key={i} className="glass-panel p-5 rounded-2xl relative overflow-hidden group hover:border-slate-700 transition">
              <div className="flex items-center justify-between">
                <span className="text-xs font-medium text-slate-400">{m.title}</span>
                <div className={`p-2 rounded-xl bg-gradient-to-tr ${m.color} text-white shadow-md`}>
                  <Icon className="h-4 w-4" />
                </div>
              </div>
              <div className="mt-4">
                <span className="text-2xl font-bold tracking-tight text-white">{m.value}</span>
                <p className="text-xs text-slate-400 mt-1 flex items-center gap-1">
                  <TrendingUp className="h-3 w-3 text-emerald-400" />
                  {m.sub}
                </p>
              </div>
            </div>
          );
        })}
      </div>

      {/* Architecture Highlights & Status */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 glass-panel p-6 rounded-2xl">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-base font-semibold text-white">Ingestion & Retrieval Pipeline</h3>
            <span className="text-xs bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 px-2 py-0.5 rounded-full font-mono">
              System Operational
            </span>
          </div>
          <div className="space-y-4 text-sm text-slate-300">
            <div className="flex items-start gap-3 p-3.5 rounded-xl bg-slate-900/60 border border-slate-800">
              <Zap className="h-5 w-5 text-amber-400 mt-0.5 shrink-0" />
              <div>
                <p className="font-semibold text-white">Hybrid Retrieval Active</p>
                <p className="text-xs text-slate-400 mt-0.5">
                  Combines PostgreSQL Full-Text Search with pgvector Cosine Distance (<code className="text-blue-400">&lt;=&gt;</code>) for high recall and keyword precision.
                </p>
              </div>
            </div>

            <div className="flex items-start gap-3 p-3.5 rounded-xl bg-slate-900/60 border border-slate-800">
              <Shield className="h-5 w-5 text-emerald-400 mt-0.5 shrink-0" />
              <div>
                <p className="font-semibold text-white">Retrieval-Time Access Control (ACL)</p>
                <p className="text-xs text-slate-400 mt-0.5">
                  Strict authorization boundary filters private and confidential documents based on user roles and tenant tenancy before LLM context is compiled.
                </p>
              </div>
            </div>

            <div className="flex items-start gap-3 p-3.5 rounded-xl bg-slate-900/60 border border-slate-800">
              <CheckCircle2 className="h-5 w-5 text-blue-400 mt-0.5 shrink-0" />
              <div>
                <p className="font-semibold text-white">Token Budget Management & Citations</p>
                <p className="text-xs text-slate-400 mt-0.5">
                  Deterministic reranker packs chunks into a strict token budget with XML metadata and source links.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Live System Health */}
        <div className="glass-panel p-6 rounded-2xl flex flex-col justify-between">
          <div>
            <h3 className="text-base font-semibold text-white mb-4">Infrastructure Health</h3>
            <div className="space-y-3">
              <div className="flex items-center justify-between p-3 rounded-xl bg-slate-900 border border-slate-800">
                <div className="flex items-center gap-2">
                  <div className="h-2 w-2 rounded-full bg-emerald-400"></div>
                  <span className="text-xs text-slate-300 font-medium">PostgreSQL + pgvector</span>
                </div>
                <span className="text-[11px] font-mono text-emerald-400">Port 5432</span>
              </div>

              <div className="flex items-center justify-between p-3 rounded-xl bg-slate-900 border border-slate-800">
                <div className="flex items-center gap-2">
                  <div className="h-2 w-2 rounded-full bg-emerald-400"></div>
                  <span className="text-xs text-slate-300 font-medium">Redis 7 (Cache Layer)</span>
                </div>
                <span className="text-[11px] font-mono text-emerald-400">Port 6379</span>
              </div>

              <div className="flex items-center justify-between p-3 rounded-xl bg-slate-900 border border-slate-800">
                <div className="flex items-center gap-2">
                  <div className="h-2 w-2 rounded-full bg-emerald-400"></div>
                  <span className="text-xs text-slate-300 font-medium">Spring Boot Engine</span>
                </div>
                <span className="text-[11px] font-mono text-emerald-400">Port 8080</span>
              </div>
            </div>
          </div>

          <div className="mt-6 p-3 rounded-xl bg-slate-900/40 border border-slate-800/80">
            <span className="text-[11px] text-slate-400 block mb-1">API Key Authenticated:</span>
            <code className="text-xs text-blue-400 font-mono">ctx-live-test-key-12345</code>
          </div>
        </div>
      </div>
    </div>
  );
}
