"use client";

import React, { useState } from "react";
import { Search, Shield, FileText, ExternalLink, Hash, Clock } from "lucide-react";

interface DocItem {
  id: string;
  title: string;
  sourceType: string;
  sourceUrl: string;
  checksum: string;
  chunksCount: number;
  visibility: "PUBLIC" | "TEAM" | "PRIVATE";
  updatedAt: string;
}

export default function KnowledgeExplorerPage() {
  const [searchTerm, setSearchTerm] = useState("");

  const docs: DocItem[] = [
    {
      id: "doc-1",
      title: "APOLLO-421: Payment Gateway Timeout Root Cause Analysis",
      sourceType: "JIRA",
      sourceUrl: "https://jira.company.internal/browse/APOLLO-421",
      checksum: "a4f8d2b7e9...",
      chunksCount: 8,
      visibility: "TEAM",
      updatedAt: "2 hours ago",
    },
    {
      id: "doc-2",
      title: "pull-requests/981: Fix idempotency key in stripe webhook",
      sourceType: "GITHUB",
      sourceUrl: "https://github.com/company/payments/pull/981",
      checksum: "7c1e30a5fb...",
      chunksCount: 14,
      visibility: "PUBLIC",
      updatedAt: "Yesterday",
    },
    {
      id: "doc-3",
      title: "Q3 Financial Discrepancies and Audit Ledger",
      sourceType: "LOCAL",
      sourceUrl: "file:///opt/docs/finance/q3_audit.pdf",
      checksum: "9d8e72c10a...",
      chunksCount: 22,
      visibility: "PRIVATE",
      updatedAt: "3 days ago",
    },
  ];

  const filtered = docs.filter((d) =>
    d.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
    d.sourceType.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6 max-w-7xl mx-auto">
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold tracking-tight text-white">Knowledge Explorer</h2>
          <p className="text-sm text-slate-400 mt-1">
            Inspect all normalized documents, checksum change detections, and ACL permissions.
          </p>
        </div>

        <div className="relative w-full md:w-80">
          <Search className="absolute left-3.5 top-3 h-4 w-4 text-slate-500" />
          <input
            type="text"
            placeholder="Search documents or checksums..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-10 pr-4 py-2 bg-slate-900/90 border border-slate-800 rounded-xl text-xs text-white placeholder-slate-500 focus:outline-none focus:border-blue-500 transition"
          />
        </div>
      </div>

      <div className="glass-panel rounded-2xl overflow-hidden border border-slate-800">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-slate-900/80 text-slate-400 border-b border-slate-800 uppercase tracking-wider font-mono">
              <tr>
                <th className="py-3.5 px-5">Document Title</th>
                <th className="py-3.5 px-4">Source</th>
                <th className="py-3.5 px-4">Checksum (SHA-256)</th>
                <th className="py-3.5 px-4">Chunks</th>
                <th className="py-3.5 px-4">Access Control</th>
                <th className="py-3.5 px-4">Updated</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800/80">
              {filtered.map((d) => (
                <tr key={d.id} className="hover:bg-slate-800/40 transition">
                  <td className="py-4 px-5">
                    <div className="font-semibold text-white flex items-center gap-2">
                      <FileText className="h-4 w-4 text-blue-400 shrink-0" />
                      <span>{d.title}</span>
                    </div>
                    <a
                      href={d.sourceUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="text-[11px] text-slate-500 hover:text-blue-400 flex items-center gap-1 mt-1 font-mono"
                    >
                      {d.sourceUrl} <ExternalLink className="h-2.5 w-2.5" />
                    </a>
                  </td>
                  <td className="py-4 px-4 font-mono text-slate-300">{d.sourceType}</td>
                  <td className="py-4 px-4 font-mono text-slate-400 flex items-center gap-1">
                    <Hash className="h-3 w-3 text-slate-500" />
                    {d.checksum}
                  </td>
                  <td className="py-4 px-4 font-mono text-slate-200">{d.chunksCount} chunks</td>
                  <td className="py-4 px-4">
                    <span
                      className={`inline-flex items-center gap-1 font-mono text-[10px] px-2 py-0.5 rounded-full border ${
                        d.visibility === "PRIVATE"
                          ? "bg-rose-500/10 text-rose-400 border-rose-500/20"
                          : d.visibility === "TEAM"
                          ? "bg-amber-500/10 text-amber-400 border-amber-500/20"
                          : "bg-emerald-500/10 text-emerald-400 border-emerald-500/20"
                      }`}
                    >
                      <Shield className="h-2.5 w-2.5" />
                      {d.visibility}
                    </span>
                  </td>
                  <td className="py-4 px-4 text-slate-400 flex items-center gap-1">
                    <Clock className="h-3 w-3 text-slate-500" />
                    {d.updatedAt}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
