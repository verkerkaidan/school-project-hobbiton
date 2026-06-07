"use client";

import React, { useState } from "react";
import { X } from "lucide-react";
import { Modal } from "@/components/modals/Modal";
import { createBugReport } from "@/lib/bugReportClient";

interface BugReportModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export function BugReportModal({ isOpen, onClose }: BugReportModalProps) {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [submitted, setSubmitted] = useState(false);

  function reset() {
    setTitle("");
    setDescription("");
    setError(null);
    setSubmitted(false);
    setLoading(false);
  }

  function handleClose() {
    reset();
    onClose();
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();

    const trimmedTitle = title.trim();
    const trimmedDesc = description.trim();

    if (!trimmedTitle || !trimmedDesc) return;

    setLoading(true);
    setError(null);

    try {
      await createBugReport(trimmedTitle, trimmedDesc);
      setSubmitted(true);
    } catch {
      setError("Something went wrong. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <Modal isOpen={isOpen} onClose={handleClose}>
      <div className="flex flex-col gap-6">
        {/* Header */}
        <div className="flex items-start justify-between gap-4">
          <div>
            <h2 className="text-lg font-semibold text-accent">Report a bug</h2>
            <p className="text-sm text-accent/60 mt-1">
              Describe the issue you encountered and we’ll look into it.
            </p>
          </div>

          <button
            onClick={handleClose}
            className="text-accent/60 hover:text-accent transition-colors"
            aria-label="Close"
          >
            <X size={18} />
          </button>
        </div>

        {submitted ? (
          /* Success state */
          <div className="flex flex-col items-center gap-3 py-8 text-center">
            <div className="w-10 h-10 rounded-full bg-brand-muted border border-brand/20 flex items-center justify-center text-brand-light text-lg">
              ✓
            </div>

            <p className="text-accent font-medium">Report submitted</p>

            <p className="text-sm text-accent/60">
              Thanks for letting us know. We’ll review it shortly.
            </p>

            <button
              onClick={handleClose}
              className="mt-2 px-4 py-2 text-xs font-medium rounded-lg bg-background-secondary border border-accent/15 text-accent hover:bg-accent/5 hover:border-accent/25 transition-colors"
            >
              Close
            </button>
          </div>
        ) : (
          /* Form */
          <form onSubmit={handleSubmit} className="flex flex-col gap-5">
            {/* Title */}
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-medium text-accent/70">
                Title
              </label>

              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                placeholder="Brief description of the issue"
                maxLength={255}
                required
                className="w-full bg-background-secondary border border-accent/15 rounded-lg px-3 py-2 text-sm text-accent placeholder:text-accent/40 focus:outline-none focus:border-accent/30"
              />
            </div>

            {/* Description */}
            <div className="flex flex-col gap-1.5">
              <label className="text-xs font-medium text-accent/70">
                Description
              </label>

              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="What happened? What did you expect to happen?"
                rows={5}
                required
                className="w-full bg-background-secondary border border-accent/15 rounded-lg px-3 py-2 text-sm text-accent placeholder:text-accent/40 focus:outline-none focus:border-accent/30 resize-none"
              />
            </div>

            {/* Error */}
            {error && (
              <p className="text-xs text-rose-400 bg-rose-500/10 border border-rose-500/20 rounded-lg px-3 py-2">
                {error}
              </p>
            )}

            {/* Actions */}
            <div className="flex justify-end gap-2">
              <button
                type="button"
                onClick={handleClose}
                className="px-4 py-2 text-xs font-medium rounded-lg text-accent/70 hover:text-accent border border-transparent hover:border-accent/20 hover:bg-accent/5 transition-colors"
              >
                Cancel
              </button>

              <button
                type="submit"
                disabled={loading || !title.trim() || !description.trim()}
                className="px-4 py-2 text-xs font-medium rounded-lg bg-brand-muted border border-brand/20 text-brand-light hover:bg-brand/10 hover:border-brand/30 transition-colors disabled:opacity-40 disabled:cursor-not-allowed"
              >
                {loading ? "Submitting..." : "Submit report"}
              </button>
            </div>
          </form>
        )}
      </div>
    </Modal>
  );
}
