"use client";

import React, { useState, useEffect } from "react";
import { createPortal } from "react-dom";
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
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    return () => setMounted(false);
  }, []);

  const handleConfirm = async () => {
    setSubmitting(true);
    try {
      await onConfirm(reason, description);
      setDescription("");
      setReason("HARASSMENT");
      onClose();
    } finally {
      setSubmitting(false);
    }
  };

  const handleClose = () => {
    if (!submitting) {
      setDescription("");
      setReason("HARASSMENT");
      setDropdownOpen(false);
      onClose();
    }
  };

  if (!mounted) return null;

  const currentReasonLabel = REPORT_REASONS.find(r => r.value === reason)?.label || "Harassment";

  return createPortal(
      <AnimatePresence>
        {isOpen && (
            <div className="fixed inset-0 z-[99999] flex items-center justify-center p-4 pointer-events-auto">

              {/* Backdrop */}
              <motion.div
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  exit={{ opacity: 0 }}
                  onClick={handleClose}
                  className="fixed inset-0 bg-black/70 backdrop-blur-sm"
              />

              {/* Modal Card */}
              <motion.div
                  initial={{ opacity: 0, scale: 0.95, y: 20 }}
                  animate={{ opacity: 1, scale: 1, y: 0 }}
                  exit={{ opacity: 0, scale: 0.95, y: 20 }}
                  transition={{ type: "spring", duration: 0.3 }}
                  className="relative w-full max-w-md mx-auto max-h-[calc(100vh-3rem)] overflow-y-auto glass-card rounded-3xl overflow-hidden shadow-2xl z-10"
              >
                {/* Header */}
                <div className="flex items-center justify-between p-6 border-b border-neutral-500/10">
                  <h2 className="rm-title">
                    Report <span className="text-brand italic">@{targetUsername}</span>
                  </h2>
                  <button
                      onClick={handleClose}
                      disabled={submitting}
                      className="rm-close-btn"
                  >
                    <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="20"
                        height="20"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        strokeWidth="2.5"
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

                  {/* Custom Dynamic Dropdown (Gelijk aan CreatePostModal) */}
                  <div className="relative">
                    <label className="rm-label">Reason</label>
                    <button
                        type="button"
                        disabled={submitting}
                        onClick={() => setDropdownOpen(!dropdownOpen)}
                        className="rm-dropdown-trigger"
                    >
                      <span>{currentReasonLabel}</span>
                      <motion.span
                          animate={{ rotate: dropdownOpen ? 180 : 0 }}
                          transition={{ duration: 0.15 }}
                          className="text-xs opacity-60"
                      >
                        ▼
                      </motion.span>
                    </button>

                    <AnimatePresence>
                      {dropdownOpen && (
                          <motion.div
                              initial={{ opacity: 0, y: -8 }}
                              animate={{ opacity: 1, y: 0 }}
                              exit={{ opacity: 0, y: -8 }}
                              transition={{ duration: 0.15 }}
                              className="rm-dropdown-menu"
                          >
                            {REPORT_REASONS.map((opt) => (
                                <button
                                    key={opt.value}
                                    type="button"
                                    onClick={() => {
                                      setReason(opt.value);
                                      setDropdownOpen(false);
                                    }}
                                    className="rm-dropdown-item"
                                >
                                  {opt.label}
                                </button>
                            ))}
                          </motion.div>
                      )}
                    </AnimatePresence>
                  </div>

                  {/* Description */}
                  <div>
                    <label className="rm-label">
                      Description{" "}
                      <span className="rm-input-hint">(optional)</span>
                    </label>
                    <textarea
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        disabled={submitting}
                        rows={4}
                        placeholder="Provide additional context..."
                        className="rm-textarea"
                    />
                  </div>
                </div>

                {/* Footer */}
                <div className="flex gap-3 px-6 pb-6">
                  <button
                      type="button"
                      onClick={handleClose}
                      disabled={submitting}
                      className="rm-btn-cancel"
                  >
                    Cancel
                  </button>
                  <button
                      type="button"
                      onClick={handleConfirm}
                      disabled={submitting}
                      className="rm-btn-submit"
                  >
                    {submitting ? "Submitting..." : "Submit Report"}
                  </button>
                </div>
              </motion.div>
            </div>
        )}
      </AnimatePresence>,
      document.body
  );
};