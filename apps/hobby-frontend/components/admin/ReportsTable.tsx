"use client";

import React, { useEffect, useState, useCallback } from "react";
import Link from "next/link";
import { motion, AnimatePresence } from "framer-motion";
import {
  RefreshCw,
  Trash2,
  ChevronDown,
  AlertCircle,
  InboxIcon,
  Users,
  FileText,
} from "lucide-react";
import { getReportsByStatus, updateReportStatus } from "@/lib/adminClient";
import { getProfile } from "@/queries/getProfile";
import { DeleteConfirmModal } from "@/components/admin/DeleteConfirmModal";
import type { Report, ReportStatus } from "@/types/reports";
import { REASON_LABELS, STATUS_CONFIG, ALL_STATUSES } from "@/types/reports";

type ReportTypeTab = "users" | "posts";
type FilterTab = ReportStatus | "ALL";

const TABS: { value: FilterTab; label: string }[] = [
  { value: "ALL", label: "All" },
  { value: "PENDING", label: "Pending" },
  { value: "UNDER_REVIEW", label: "Under Review" },
  { value: "RESOLVED", label: "Resolved" },
  { value: "DISMISSED", label: "Dismissed" },
  { value: "ACTION_TAKEN", label: "Action Taken" },
];

interface EnrichedReport extends Report {
  initiatorUsername?: string;
  targetUsername?: string;
}

async function enrichReports(reports: Report[]): Promise<EnrichedReport[]> {
  const uniqueIds = [
    ...new Set([
      ...reports.map((r) => r.initiator),
      ...reports.map((r) => r.target),
    ]),
  ];

  const profileMap: Record<string, string> = {};
  await Promise.allSettled(
    uniqueIds.map(async (id) => {
      const p = await getProfile(id);
      profileMap[id] = p.username;
    }),
  );

  return reports.map((r) => ({
    ...r,
    initiatorUsername: profileMap[r.initiator] ?? r.initiator.slice(0, 8) + "…",
    targetUsername: profileMap[r.target] ?? r.target.slice(0, 8) + "…",
  }));
}

export const ReportsTable = () => {
  const [reportType, setReportType] = useState<ReportTypeTab>("users");
  const [tab, setTab] = useState<FilterTab>("PENDING");
  const [reports, setReports] = useState<EnrichedReport[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [updatingId, setUpdatingId] = useState<string | null>(null);

  // Delete modal state
  const [deleteTarget, setDeleteTarget] = useState<{
    userId: string;
    username: string;
  } | null>(null);

  const fetchReports = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const raw = await getReportsByStatus(tab);
      const enriched = await enrichReports(raw);
      setReports(enriched);
    } catch {
      setError("Failed to load reports. Check your admin service URLs.");
    } finally {
      setLoading(false);
    }
  }, [tab]);

  useEffect(() => {
    fetchReports();
  }, [fetchReports]);

  const handleStatusChange = async (reportId: string, status: ReportStatus) => {
    setUpdatingId(reportId);
    try {
      await updateReportStatus(reportId, status);
      setReports((prev) =>
        prev.map((r) => (r.id === reportId ? { ...r, status } : r)),
      );
    } catch {
      // silently revert if needed
    } finally {
      setUpdatingId(null);
    }
  };

  return (
    <div className="space-y-5">
      {/* ─── Report Type Tabs (User / Post) ─── */}
      <div className="flex gap-2">
        {(
          [
            { value: "users" as const, label: "User Reports", icon: Users },
            { value: "posts" as const, label: "Post Reports", icon: FileText },
          ] satisfies {
            value: ReportTypeTab;
            label: string;
            icon: React.ElementType;
          }[]
        ).map(({ value, label, icon: Icon }) => (
          <button
            key={value}
            onClick={() => setReportType(value)}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium border transition-all duration-200 ${
              reportType === value
                ? "bg-brand-muted border-brand/20 text-brand-light"
                : "bg-white/3 border-white/8 text-white/40 hover:text-white/70 hover:border-white/15"
            }`}
          >
            <Icon className="w-4 h-4" />
            {label}
          </button>
        ))}
      </div>

      <AnimatePresence mode="wait">
        {/* ═══════════════════════════════════════════
            USER REPORTS
        ═══════════════════════════════════════════ */}
        {reportType === "users" && (
          <motion.div
            key="user-reports"
            initial={{ opacity: 0, y: 6 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 6 }}
            className="space-y-5"
          >
            {/* Status filter tabs */}
            <div className="flex items-center gap-1 p-1 rounded-xl bg-white/3 border border-white/5 w-fit flex-wrap">
              {TABS.map((t) => (
                <button
                  key={t.value}
                  onClick={() => setTab(t.value)}
                  className={`relative px-3 py-1.5 rounded-lg text-xs font-medium transition-all duration-200 ${
                    tab === t.value
                      ? "text-white"
                      : "text-white/40 hover:text-white/70"
                  }`}
                >
                  {tab === t.value && (
                    <motion.div
                      layoutId="reportTabBg"
                      className="absolute inset-0 bg-white/8 rounded-lg border border-white/10"
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
                className="ml-2 w-7 h-7 flex items-center justify-center rounded-lg text-white/30 hover:text-white/70 hover:bg-white/5 transition-colors disabled:opacity-30"
                title="Refresh"
              >
                <RefreshCw
                  className={`w-3.5 h-3.5 ${loading ? "animate-spin" : ""}`}
                />
              </button>
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

            {/* Table */}
            <div className="rounded-2xl border border-white/8 overflow-hidden">
              <div className="grid grid-cols-[1.2fr_1.2fr_1fr_2fr_1fr_0.9fr_auto] gap-4 px-5 py-3 bg-white/3 border-b border-white/5 text-xs font-medium text-white/30 uppercase tracking-wider">
                <span>Reporter</span>
                <span>Reported User</span>
                <span>Reason</span>
                <span>Description</span>
                <span>Status</span>
                <span>Date</span>
                <span className="w-24 text-right">Actions</span>
              </div>

              <AnimatePresence mode="wait">
                {loading ? (
                  <motion.div
                    key="skeleton"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="divide-y divide-white/4"
                  >
                    {[...Array(5)].map((_, i) => (
                      <div
                        key={i}
                        className="grid grid-cols-[1.2fr_1.2fr_1fr_2fr_1fr_0.9fr_auto] gap-4 px-5 py-4 animate-pulse"
                      >
                        {[...Array(6)].map((_, j) => (
                          <div key={j} className="h-4 rounded-md bg-white/5" />
                        ))}
                        <div className="w-24 h-4 rounded-md bg-white/5" />
                      </div>
                    ))}
                  </motion.div>
                ) : reports.length === 0 ? (
                  <motion.div
                    key="empty"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="flex flex-col items-center justify-center py-16 gap-3 text-white/20"
                  >
                    <InboxIcon className="w-10 h-10 opacity-40" />
                    <p className="text-sm">No reports found for this filter.</p>
                  </motion.div>
                ) : (
                  <motion.div
                    key="rows"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="divide-y divide-white/4"
                  >
                    {reports.map((report) => (
                      <ReportRow
                        key={report.id}
                        report={report}
                        isUpdating={updatingId === report.id}
                        onStatusChange={handleStatusChange}
                        onDeletePosts={() =>
                          setDeleteTarget({
                            userId: report.target,
                            username: report.targetUsername ?? report.target,
                          })
                        }
                      />
                    ))}
                  </motion.div>
                )}
              </AnimatePresence>
            </div>

            {!loading && reports.length > 0 && (
              <p className="text-white/20 text-xs px-1">
                {reports.length} report{reports.length !== 1 ? "s" : ""} shown
              </p>
            )}
          </motion.div>
        )}

        {/* ═══════════════════════════════════════════
            POST REPORTS — coming soon
        ═══════════════════════════════════════════ */}
        {reportType === "posts" && (
          <motion.div
            key="post-reports"
            initial={{ opacity: 0, y: 6 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: 6 }}
            className="flex flex-col items-center justify-center py-24 gap-4 text-white/20 rounded-2xl border border-white/8 border-dashed"
          >
            <FileText className="w-10 h-10 opacity-30" />
            <div className="text-center">
              <p className="text-sm font-medium text-white/30">
                Post reporting coming soon
              </p>
              <p className="text-xs mt-1 text-white/20">
                Post-level reports will appear here once the feature is enabled.
              </p>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {/* ─── Delete Modal ─── */}
      <DeleteConfirmModal
        isOpen={!!deleteTarget}
        targetUserId={deleteTarget?.userId ?? ""}
        targetUsername={deleteTarget?.username ?? ""}
        onClose={() => setDeleteTarget(null)}
        onActionComplete={() => {
          setDeleteTarget(null);
          fetchReports();
        }}
      />
    </div>
  );
};

/* ─────────────────────────────────────────────────────────── */
/*  Single report row                                          */
/* ─────────────────────────────────────────────────────────── */

interface ReportRowProps {
  report: EnrichedReport;
  isUpdating: boolean;
  onStatusChange: (id: string, status: ReportStatus) => void;
  onDeletePosts: () => void;
}

const ReportRow = ({
  report,
  isUpdating,
  onStatusChange,
  onDeletePosts,
}: ReportRowProps) => {
  const statusCfg = STATUS_CONFIG[report.status];
  const formattedDate = report.createdAt
    ? new Date(report.createdAt).toLocaleDateString("en-GB", {
        day: "2-digit",
        month: "short",
        year: "2-digit",
      })
    : "—";

  return (
    <motion.div
      layout
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      className="grid grid-cols-[1.2fr_1.2fr_1fr_2fr_1fr_0.9fr_auto] gap-4 px-5 py-4 hover:bg-white/2 transition-colors group items-center"
    >
      {/* Reporter */}
      <Link
        href={`/profile/${report.initiatorUsername}`}
        className="text-sm text-white/70 font-medium truncate hover:text-white transition-colors underline-offset-2 hover:underline"
        title={`View @${report.initiatorUsername}'s profile`}
      >
        @{report.initiatorUsername}
      </Link>

      {/* Reported User */}
      <Link
        href={`/profile/${report.targetUsername}`}
        className="text-sm text-white font-medium truncate hover:text-brand-light transition-colors underline-offset-2 hover:underline"
        title={`View @${report.targetUsername}'s profile`}
      >
        @{report.targetUsername}
      </Link>

      {/* Reason */}
      <span className="text-xs text-white/50 truncate">
        {REASON_LABELS[report.reason]}
      </span>

      {/* Description */}
      <span
        className="text-xs text-white/40 truncate"
        title={report.description}
      >
        {report.description || (
          <em className="italic text-white/20">No description</em>
        )}
      </span>

      {/* Status */}
      <div>
        <span
          className={`inline-flex items-center px-2 py-0.5 rounded-full text-[11px] font-medium ${statusCfg.className}`}
        >
          {statusCfg.label}
        </span>
      </div>

      {/* Date */}
      <span className="text-xs text-white/30">{formattedDate}</span>

      {/* Actions */}
      <div className="flex items-center gap-2 w-24 justify-end">
        {/* Status select */}
        <div className="relative">
          <select
            value={report.status}
            disabled={isUpdating}
            onChange={(e) =>
              onStatusChange(report.id, e.target.value as ReportStatus)
            }
            className="appearance-none pl-2.5 pr-6 py-1.5 rounded-lg bg-white/5 border border-white/10 text-white/60 text-xs focus:outline-none focus:border-brand/40 hover:border-white/20 transition-colors cursor-pointer disabled:opacity-40"
            title="Change status"
          >
            {ALL_STATUSES.map((s) => (
              <option key={s} value={s} className="bg-[#0d0d0f]">
                {STATUS_CONFIG[s].label}
              </option>
            ))}
          </select>
          <ChevronDown className="absolute right-1.5 top-1/2 -translate-y-1/2 w-3 h-3 text-white/30 pointer-events-none" />
        </div>

        {/* Delete posts button */}
        <button
          onClick={onDeletePosts}
          className="w-7 h-7 flex items-center justify-center rounded-lg text-white/20 hover:text-rose-400 hover:bg-rose-500/10 transition-colors"
          title={`Delete posts by @${report.targetUsername}`}
        >
          <Trash2 className="w-3.5 h-3.5" />
        </button>
      </div>
    </motion.div>
  );
};
