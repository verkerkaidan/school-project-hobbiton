"use client";

import React, { useCallback, useEffect, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import {
  AlertCircle,
  Bug,
  InboxIcon,
  RefreshCw,
  Search,
  X,
} from "lucide-react";

import { getAllBugReports, closeBugReport } from "@/lib/bugReportClient";
import type { BugReport } from "@/lib/bugReportClient";

type StatusFilter = "OPEN" | "ALL";

const STATUS_TABS: { value: StatusFilter; label: string }[] = [
  { value: "OPEN", label: "Open" },
  { value: "ALL", label: "All" },
];

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString("en-GB", {
    day: "2-digit",
    month: "short",
    year: "2-digit",
  });
}

export const BugReportsTable = () => {
  const [tab, setTab] = useState<StatusFilter>("OPEN");
  const [reports, setReports] = useState<BugReport[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const [closing, setClosing] = useState<string | null>(null);

  const fetchReports = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const status = tab === "OPEN" ? "OPEN" : undefined;
      const data = await getAllBugReports(status);

      setReports(data ?? []);
    } catch {
      setError("Failed to load bug reports.");
    } finally {
      setLoading(false);
    }
  }, [tab]);

  useEffect(() => {
    fetchReports();
  }, [fetchReports]);

  const filtered = reports.filter((report) => {
    const q = search.trim().toLowerCase();

    if (!q) return true;

    return (
      report.title.toLowerCase().includes(q) ||
      report.description.toLowerCase().includes(q)
    );
  });

  const handleClose = async (id: string) => {
    setClosing(id);

    try {
      await closeBugReport(id);

      setReports((prev) =>
        prev.map((r) =>
          r.id === id
            ? {
                ...r,
                status: "CLOSED" as const,
              }
            : r,
        ),
      );
    } catch {
      await fetchReports();
    } finally {
      setClosing(null);
    }
  };

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-xl bg-brand-muted border border-brand/20 flex items-center justify-center">
          <Bug className="w-5 h-5 text-brand-light" />
        </div>

        <div>
          <h2 className="text-base font-semibold text-accent">Bug Reports</h2>
          <p className="text-xs text-accent/60">Issues submitted by users</p>
        </div>
      </div>

      {/* Filters */}
      <div className="flex flex-col lg:flex-row gap-3 lg:items-center">
        {/* Status Tabs */}
        <div className="flex items-center gap-1 p-1 rounded-xl bg-background-secondary border border-accent/15 w-fit">
          {STATUS_TABS.map((t) => (
            <button
              key={t.value}
              onClick={() => setTab(t.value)}
              className={`relative px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${
                tab === t.value
                  ? "text-accent"
                  : "text-accent/60 hover:text-accent"
              }`}
            >
              {tab === t.value && (
                <motion.div
                  layoutId="bugReportTab"
                  className="absolute inset-0 rounded-lg bg-accent/10 border border-accent/20"
                  transition={{
                    type: "spring",
                    stiffness: 400,
                    damping: 30,
                  }}
                />
              )}

              <span className="relative">{t.label}</span>
            </button>
          ))}

          <button
            onClick={fetchReports}
            disabled={loading}
            className="ml-1 w-7 h-7 flex items-center justify-center rounded-lg text-accent/60 hover:text-accent hover:bg-accent/10 transition-colors disabled:opacity-40"
            title="Refresh"
          >
            <RefreshCw
              className={`w-3.5 h-3.5 ${loading ? "animate-spin" : ""}`}
            />
          </button>
        </div>

        {/* Search */}
        <div className="relative flex-1">
          <Search
            size={14}
            className="absolute left-3 top-1/2 -translate-y-1/2 text-accent/50"
          />

          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search title or description..."
            className="w-full bg-background-secondary border border-accent/15 rounded-xl pl-9 pr-9 py-2.5 text-sm text-accent placeholder:text-accent/40 focus:outline-none focus:border-accent/30"
          />

          {search && (
            <button
              onClick={() => setSearch("")}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-accent/50 hover:text-accent transition-colors"
            >
              <X size={14} />
            </button>
          )}
        </div>
      </div>

      {/* Error */}
      <AnimatePresence>
        {error && (
          <motion.div
            initial={{ opacity: 0, y: -6 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -6 }}
            className="flex items-center gap-2 px-4 py-3 rounded-xl bg-rose-500/10 border border-rose-500/20 text-rose-400 text-sm"
          >
            <AlertCircle className="w-4 h-4 shrink-0" />
            {error}
          </motion.div>
        )}
      </AnimatePresence>

      {/* List Container */}
      <div className="rounded-2xl border border-accent/15 bg-background-secondary overflow-hidden">
        <AnimatePresence mode="wait">
          {loading ? (
            <motion.div
              key="loading"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="divide-y divide-accent/10"
            >
              {[...Array(5)].map((_, i) => (
                <div key={i} className="p-5 animate-pulse space-y-3">
                  <div className="w-48 h-4 bg-accent/10 rounded-md" />
                  <div className="w-full h-3 bg-accent/10 rounded-md" />
                  <div className="w-2/3 h-3 bg-accent/10 rounded-md" />
                </div>
              ))}
            </motion.div>
          ) : filtered.length === 0 ? (
            <motion.div
              key="empty"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="flex flex-col items-center justify-center py-20 gap-4 text-accent/50"
            >
              <InboxIcon className="w-10 h-10 opacity-30" />

              <div className="text-center">
                <p className="text-sm font-medium">
                  {search
                    ? "No reports match your search."
                    : "No bug reports found."}
                </p>

                <p className="text-xs mt-1 text-accent/55">
                  New submissions will appear here.
                </p>
              </div>
            </motion.div>
          ) : (
            <motion.div
              key="rows"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="divide-y divide-accent/10"
            >
              {filtered.map((report) => (
                <motion.div
                  key={report.id}
                  layout
                  className="p-5 hover:bg-accent/5 transition-colors"
                >
                  <div className="flex items-center justify-between gap-4">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <h3 className="text-sm font-medium text-accent">
                          {report.title}
                        </h3>

                        <span
                          className={`inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-medium ${
                            report.status === "OPEN"
                              ? "bg-amber-500/10 text-amber-400 border border-amber-500/20"
                              : "bg-accent/10 text-accent/70 border border-accent/15"
                          }`}
                        >
                          {report.status}
                        </span>
                      </div>

                      <p className="mt-2 text-sm text-accent/65 whitespace-pre-wrap">
                        {report.description}
                      </p>

                      <div className="mt-3 flex flex-wrap gap-2 text-xs text-accent/50">
                        <span>Reported {formatDate(report.createdAt)}</span>

                        {report.closedAt && (
                          <span>• Closed {formatDate(report.closedAt)}</span>
                        )}
                      </div>
                    </div>

                    {report.status === "OPEN" && (
                      <button
                        onClick={() => handleClose(report.id)}
                        disabled={closing === report.id}
                        className="shrink-0 px-3 py-2 rounded-lg text-xs font-medium border border-accent/20 bg-background hover:bg-accent/10 hover:border-accent/30 text-accent transition-colors disabled:opacity-40"
                      >
                        {closing === report.id ? "Closing..." : "Close"}
                      </button>
                    )}
                  </div>
                </motion.div>
              ))}
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {!loading && filtered.length > 0 && (
        <p className="text-xs text-accent/55 px-1">
          {filtered.length} report
          {filtered.length !== 1 ? "s" : ""} shown
        </p>
      )}
    </div>
  );
};
