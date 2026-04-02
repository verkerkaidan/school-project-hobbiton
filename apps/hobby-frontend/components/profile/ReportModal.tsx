"use client";

import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";

const REPORT_REASONS = [
  { value: "HARASSMENT", label: "Harassment" },
  { value: "HATE_SPEECH", label: "Hate Speech" },
  { value: "SPAM", label: "Spam" },
  { value: "INAPPROPRIATE_CONTENT", label: "Inappropriate Content" },
  { value: "MISINFORMATION", label: "Misinformation" },
  { value: "IMPERSONATION", label: "Impersonation" },
  { value: "SCAM_FRAUD", label: "Scam or Fraud" },
  { value: "VIOLENCE", label: "Violence" },
  { value: "SEXUAL_CONTENT", label: "Sexual Content" },
  { value: "OTHER", label: "Other" },
] as const;

type ReportReason = (typeof REPORT_REASONS)[number]["value"];

interface ReportModalProps {
  isOpen: boolean;
  targetUsername: string;
  onConfirm: (reason: ReportReason, description: string) => Promise<void>;
  onClose: () => void;
}

export const ReportModal = ({
  isOpen,
  targetUsername,
  onConfirm,
  onClose,
}: ReportModalProps) => {
  const [reason, setReason] = useState<ReportReason>("HARASSMENT");
  const [description, setDescription] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const handleConfirm = async () => {
    setSubmitting(true);
    try {
      await onConfirm(reason, description);
      // Reset form fields after a successful submission so the modal is clean if reopened
      setDescription("");
      setReason("HARASSMENT");
      onClose();
    } finally {
      setSubmitting(false);
    }
  };

  const handleClose = () => {
    // Prevent closing while a submission is in-flight to avoid orphaned requests
    if (!submitting) {
      setDescription("");
      setReason("HARASSMENT");
      onClose();
    }
  };

  return (
    <AnimatePresence>
      {isOpen && (
        <>
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={handleClose}
            className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50"
          />

          {/* Modal */}
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 20 }}
            className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-md mx-auto z-50 px-4"
          >
            <div className="glass-card rounded-3xl overflow-hidden">
              {/* Header */}
              <div className="flex items-center justify-between p-6 border-b border-white/5">
                <h2 className="text-lg font-semibold text-white">
                  Report{" "}
                  <span className="text-brand italic">{targetUsername}</span>
                </h2>
                <button
                  onClick={handleClose}
                  disabled={submitting}
                  className="text-accent hover:text-white transition-colors p-1"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    width="20"
                    height="20"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth="2"
                    strokeLinecap="round"
                    strokeLinejoin="round"
                  >
                    <line x1="18" y1="6" x2="6" y2="18" />
                    <line x1="6" y1="6" x2="18" y2="18" />
                  </svg>
                </button>
              </div>

              {/* Body */}
              <div className="p-6 space-y-5">
                {/* Reason */}
                <div>
                  <label className="block text-sm font-medium text-accent mb-2 uppercase tracking-wider">
                    Reason
                  </label>
                  <select
                    value={reason}
                    onChange={(e) => setReason(e.target.value as ReportReason)}
                    disabled={submitting}
                    className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-white text-sm focus:outline-none focus:border-brand/50 appearance-none cursor-pointer"
                  >
                    {REPORT_REASONS.map((r) => (
                      <option
                        key={r.value}
                        value={r.value}
                        className="bg-gray-900 text-white"
                      >
                        {r.label}
                      </option>
                    ))}
                  </select>
                </div>

                {/* Description */}
                <div>
                  <label className="block text-sm font-medium text-accent mb-2 uppercase tracking-wider">
                    Description{" "}
                    <span className="normal-case text-white/30">
                      (optional)
                    </span>
                  </label>
                  <textarea
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    disabled={submitting}
                    rows={4}
                    placeholder="Provide additional context..."
                    className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-white text-sm placeholder:text-white/30 focus:outline-none focus:border-brand/50 resize-none"
                  />
                </div>
              </div>

              {/* Footer */}
              <div className="flex gap-3 px-6 pb-6">
                <button
                  onClick={handleClose}
                  disabled={submitting}
                  className="flex-1 px-4 py-3 rounded-xl border border-white/10 text-accent hover:text-white hover:border-white/20 transition-colors text-sm font-medium"
                >
                  Cancel
                </button>
                <button
                  onClick={handleConfirm}
                  disabled={submitting}
                  className="flex-1 px-4 py-3 rounded-xl bg-red-500/20 border border-red-500/30 text-red-400 hover:bg-red-500/30 hover:text-red-300 transition-colors text-sm font-medium disabled:opacity-50"
                >
                  {submitting ? "Submitting..." : "Submit Report"}
                </button>
              </div>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
};
