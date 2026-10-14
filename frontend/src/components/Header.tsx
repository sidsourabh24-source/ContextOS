"use client";

import React from "react";
import { Activity, Bell, Key, Shield } from "lucide-react";

export const Header: React.FC = () => {
  return (
    <header className="h-16 border-b border-slate-800 bg-[#0b0f19]/80 backdrop-blur-md px-8 flex items-center justify-between sticky top-0 z-30">
      <div className="flex items-center gap-3">
        <span className="flex h-2.5 w-2.5 rounded-full bg-emerald-500 animate-pulse" />
        <span className="text-xs font-mono text-slate-300">Cluster: Active (pgvector + Redis Connected)</span>
      </div>

      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2 bg-slate-900 border border-slate-800 px-3 py-1.5 rounded-lg text-xs font-mono text-slate-300">
          <Key className="h-3.5 w-3.5 text-amber-400" />
          <span>API: X-ContextOS-API-Key</span>
        </div>

        <div className="h-8 w-8 rounded-full bg-gradient-to-tr from-indigo-500 to-purple-600 flex items-center justify-center text-xs font-bold text-white shadow">
          AC
        </div>
      </div>
    </header>
  );
};
