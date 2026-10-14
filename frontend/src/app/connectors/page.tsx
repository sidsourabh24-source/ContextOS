"use client";

import React, { useState } from "react";
import { GitBranch, Folder, RefreshCw, CheckCircle2, AlertCircle, Plus, ExternalLink } from "lucide-react";

interface ConnectorItem {
  id: string;
  name: string;
  type: "GITHUB" | "LOCAL" | "JIRA" | "SLACK";
  status: "ACTIVE" | "SYNCING" | "ERROR";
  lastSync: string;
  itemsCount: number;
  config: string;
}

export default function ConnectorsPage() {
  const [connectors, setConnectors] = useState<ConnectorItem[]>([
    {
      id: "c-1",
      name: "Core Engineering Repo",
      type: "GITHUB",
      status: "ACTIVE",
      lastSync: "10 mins ago",
      itemsCount: 842,
      config: '{"owner": "contextos", "repo": "core-platform"}',
    },
    {
      id: "c-2",
      name: "Company Internal Policies",
      type: "LOCAL",
      status: "ACTIVE",
      lastSync: "1 hour ago",
      itemsCount: 154,
      config: '{"path": "/opt/contextos/docs/policies"}',
    },
    {
      id: "c-3",
      name: "Apollo Release Jira Project",
      type: "JIRA",
      status: "ACTIVE",
      lastSync: "35 mins ago",
      itemsCount: 424,
      config: '{"projectKey": "APOLLO"}',
    },
  ]);

  const [syncingId, setSyncingId] = useState<string | null>(null);

  const handleSync = (id: string) => {
    setSyncingId(id);
    setTimeout(() => {
      setSyncingId(null);
    }, 1200);
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-white">Source Connectors</h2>
          <p className="text-sm text-slate-400 mt-1">
            Pluggable adapters connecting ContextOS to GitHub, local file systems, and enterprise tools.
          </p>
        </div>

        <button className="flex items-center gap-2 bg-blue-600 hover:bg-blue-500 text-white text-xs font-semibold px-4 py-2.5 rounded-xl shadow-lg shadow-blue-600/20 transition">
          <Plus className="h-4 w-4" />
          Add Connector
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {connectors.map((c) => (
          <div key={c.id} className="glass-panel p-5 rounded-2xl flex flex-col justify-between hover:border-slate-700 transition">
            <div>
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-2.5">
                  <div className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 text-blue-400">
                    {c.type === "GITHUB" ? <GitBranch className="h-5 w-5" /> : <Folder className="h-5 w-5" />}
                  </div>
                  <div>
                    <h3 className="text-sm font-semibold text-white">{c.name}</h3>
                    <span className="text-[11px] font-mono text-slate-400">{c.type}</span>
                  </div>
                </div>

                <span className="flex items-center gap-1.5 text-xs font-mono text-emerald-400 bg-emerald-500/10 border border-emerald-500/20 px-2 py-0.5 rounded-full">
                  <CheckCircle2 className="h-3 w-3" />
                  {c.status}
                </span>
              </div>

              <div className="p-3 rounded-xl bg-slate-900/80 border border-slate-800/80 font-mono text-xs text-slate-300 break-all my-3">
                {c.config}
              </div>

              <div className="flex justify-between text-xs text-slate-400 py-1">
                <span>Indexed Documents:</span>
                <span className="font-semibold text-white">{c.itemsCount} docs</span>
              </div>
              <div className="flex justify-between text-xs text-slate-400 py-1">
                <span>Last Synchronized:</span>
                <span>{c.lastSync}</span>
              </div>
            </div>

            <div className="mt-5 pt-3 border-t border-slate-800/80 flex items-center justify-between">
              <button
                onClick={() => handleSync(c.id)}
                disabled={syncingId === c.id}
                className="flex items-center gap-2 text-xs font-medium text-slate-300 hover:text-white px-3 py-1.5 rounded-lg bg-slate-800/80 hover:bg-slate-700 transition"
              >
                <RefreshCw className={`h-3.5 w-3.5 ${syncingId === c.id ? "animate-spin text-blue-400" : ""}`} />
                {syncingId === c.id ? "Syncing..." : "Sync Now"}
              </button>

              <button className="text-xs text-slate-400 hover:text-slate-200 flex items-center gap-1">
                Inspect <ExternalLink className="h-3 w-3" />
              </button>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
