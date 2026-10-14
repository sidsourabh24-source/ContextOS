"use client";

import React from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { 
  Layers, 
  Database, 
  GitBranch, 
  Search, 
  BarChart3, 
  ShieldCheck, 
  Cpu
} from "lucide-react";

const NAV_ITEMS = [
  { name: "Overview", href: "/", icon: Layers },
  { name: "Connectors", href: "/connectors", icon: GitBranch },
  { name: "Knowledge Explorer", href: "/documents", icon: Database },
  { name: "Retrieval Tracer", href: "/retrieval-tracer", icon: Search },
  { name: "Evaluation & MRR", href: "/evaluation", icon: BarChart3 },
];

export const Sidebar: React.FC = () => {
  const pathname = usePathname();

  return (
    <aside className="w-64 min-h-screen bg-[#0b0f19] border-r border-slate-800 flex flex-col justify-between p-4 select-none">
      <div>
        <div className="flex items-center gap-3 px-3 py-4 mb-6">
          <div className="h-10 w-10 rounded-xl bg-gradient-to-tr from-blue-600 to-indigo-500 flex items-center justify-center shadow-lg shadow-blue-500/20">
            <Cpu className="h-6 w-6 text-white" />
          </div>
          <div>
            <h1 className="text-lg font-bold tracking-tight text-white flex items-center gap-2">
              ContextOS
              <span className="text-[10px] bg-blue-500/20 text-blue-400 border border-blue-500/30 px-1.5 py-0.5 rounded-full font-mono">v1.0</span>
            </h1>
            <p className="text-xs text-slate-400">Universal Context Layer</p>
          </div>
        </div>

        <nav className="space-y-1">
          {NAV_ITEMS.map((item) => {
            const Icon = item.icon;
            const isActive = pathname === item.href;
            return (
              <Link
                key={item.name}
                href={item.href}
                className={`flex items-center gap-3 px-3.5 py-2.5 rounded-lg text-sm font-medium transition-all ${
                  isActive
                    ? "bg-blue-600/15 text-blue-400 border border-blue-500/30 shadow-sm"
                    : "text-slate-400 hover:text-slate-200 hover:bg-slate-800/50"
                }`}
              >
                <Icon className={`h-4 w-4 ${isActive ? "text-blue-400" : "text-slate-400"}`} />
                {item.name}
              </Link>
            );
          })}
        </nav>
      </div>

      <div className="p-3 rounded-xl bg-slate-900/60 border border-slate-800/80">
        <div className="flex items-center gap-2 text-xs font-semibold text-emerald-400 mb-1">
          <ShieldCheck className="h-4 w-4" />
          <span>Tenant Isolated</span>
        </div>
        <p className="text-[11px] text-slate-400 leading-relaxed">
          PostgreSQL pgvector & ACL retrieval filters active.
        </p>
      </div>
    </aside>
  );
};
