"use client";

import React from "react";
import { 
  FileText, 
  Layers, 
  GitBranch, 
  Clock, 
  Zap, 
  CheckCircle2, 
  ArrowUpRight,
  TrendingUp,
  Shield
} from "lucide-react";

export default function OverviewPage() {
  const metrics = [
    { title: "Indexed Documents", value: "1,420", sub: "+12% this week", icon: FileText, color: "from-blue-500 to-indigo-600" },
    { title: "Total Chunks (pgvector)", value: "18,940", sub: "1536-dim embeddings", icon: Layers, color: "from-purple-500 to-pink-600" },
    { title: "Active Connectors", value: "4 Sources", sub: "GitHub, Local, Jira, Slack", icon: GitBranch, color: "from-emerald-500 to-teal-600" },
    { title: "Retrieval Latency (p95)", value: "142 ms", sub: "Hybrid FTS + pgvector", icon: Clock, color: "from-amber-500 to-orange-600" },
  ];

  return (
    <div className="space-y-8 max-w-7xl mx-auto">
      <div>
        <h2 className="text-2xl font-bold tracking-tight text-white">System Overview</h2>
        <p className="text-sm text-slate-400 mt-1">
          Real-time metrics for ContextOS knowledge indexing, pgvector similarity, and authorization pipelines.
        </p>
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
                  Filters candidates in SQL prior to reranking. Prevents unauthorized company knowledge from ever reaching the LLM prompt.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Cache & Freshness Stats */}
        <div className="glass-panel p-6 rounded-2xl flex flex-col justify-between">
          <div>
            <h3 className="text-base font-semibold text-white mb-4">Cache & Freshness</h3>
            <div className="space-y-4">
              <div>
                <div className="flex justify-between text-xs mb-1">
                  <span className="text-slate-400">Redis Query Cache Hit Rate</span>
                  <span className="font-mono text-white font-bold">42.8%</span>
                </div>
                <div className="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
                  <div className="bg-blue-500 h-full rounded-full" style={{ width: "42.8%" }} />
                </div>
              </div>

              <div>
                <div className="flex justify-between text-xs mb-1">
                  <span className="text-slate-400">Recall @ Top 5</span>
                  <span className="font-mono text-white font-bold">88.4%</span>
                </div>
                <div className="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
                  <div className="bg-emerald-500 h-full rounded-full" style={{ width: "88.4%" }} />
                </div>
              </div>

              <div>
                <div className="flex justify-between text-xs mb-1">
                  <span className="text-slate-400">Mean Reciprocal Rank (MRR)</span>
                  <span className="font-mono text-white font-bold">0.81</span>
                </div>
                <div className="w-full bg-slate-800 h-2 rounded-full overflow-hidden">
                  <div className="bg-purple-500 h-full rounded-full" style={{ width: "81%" }} />
                </div>
              </div>
            </div>
          </div>

          <div className="mt-6 pt-4 border-t border-slate-800 flex justify-between items-center text-xs text-slate-400">
            <span>Last Full Sync: 12 mins ago</span>
            <span className="text-blue-400 font-mono">0 errors</span>
          </div>
        </div>
      </div>
    </div>
  );
}
