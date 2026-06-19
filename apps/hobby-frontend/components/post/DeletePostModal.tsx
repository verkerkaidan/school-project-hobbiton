"use client";

import React, { useEffect, useState } from "react";
import { createPortal } from "react-dom";
import { motion, AnimatePresence } from "framer-motion";
import { AlertTriangle, Trash2, X } from "lucide-react";

interface DeletePostModalProps {
  isOpen: boolean;
  postTitle: string;
  onConfirm: () => Promise<void>;
  onClose: () => void;
}

export function DeletePostModal({
  isOpen,
  postTitle,
  onConfirm,
  onClose,
}: DeletePostModalProps) {
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
    return () => setMounted(false);
  }, []);

  useEffect(() => {
    if (!isOpen) {
      setError(null);
      setDeleting(false);
    }
  }, [isOpen]);

  const handleConfirm = async () => {
    setDeleting(true);
    setError(null);
    try {
      await onConfirm();
      onClose();
    } catch {
      setError("Something went wrong. Please try again.");
    } finally {
      setDeleting(false);
    }
  };

  const handleClose = () => {
    if (!deleting) onClose();
  };

  if (!mounted) return null;

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

          {/* Modal card */}
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 20 }}
            transition={{ type: "spring", duration: 0.3 }}
            className="relative w-full max-w-md mx-auto glass-card rounded-3xl overflow-hidden shadow-2xl z-10"
            onClick={(e) => e.stopPropagation()}
          >
            {/* Header */}
            <div className="flex items-center justify-between p-6 border-b border-neutral-500/10">
              <div className="flex items-center gap-3">
                <div className="w-9 h-9 rounded-xl bg-rose-500/15 border border-rose-500/20 flex items-center justify-center">
                  <Trash2 className="w-4 h-4 text-rose-400" />
                </div>
                <h2 className="text-white font-semibold text-base">Delete Post</h2>
              </div>
              <button
                onClick={handleClose}
                disabled={deleting}
                className="w-8 h-8 flex items-center justify-center rounded-xl text-accent/60 hover:text-accent hover:bg-accent/10 transition-colors disabled:opacity-40"
              >
                <X className="w-4 h-4" />
              </button>
            </div>

            {/* Body */}
            <div className="p-6 flex flex-col items-center text-center gap-5">
              <div className="w-16 h-16 rounded-2xl bg-rose-500/10 border border-rose-500/20 flex items-center justify-center">
                <AlertTriangle className="w-7 h-7 text-rose-400" />
              </div>

              <div className="space-y-2">
                <p className="text-accent/80 text-sm leading-relaxed">
                  Are you sure you want to permanently delete{" "}
                  <span className="text-white font-medium">"{postTitle}"</span>?
                </p>
                <p className="text-rose-400/80 text-xs">
                  This action cannot be undone.
                </p>
              </div>

              {error && (
                <p className="text-rose-400 text-xs">{error}</p>
              )}
            </div>

            {/* Footer */}
            <div className="flex gap-3 px-6 pb-6">
              <button
                type="button"
                onClick={handleClose}
                disabled={deleting}
                className="flex-1 px-4 py-2.5 rounded-xl border border-accent/20 text-accent/70 hover:text-accent hover:border-accent/35 transition-colors text-sm font-medium disabled:opacity-40"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleConfirm}
                disabled={deleting}
                className="flex-1 px-4 py-2.5 rounded-xl bg-rose-500/20 border border-rose-500/30 text-rose-400 hover:bg-rose-500/30 hover:text-rose-300 transition-colors text-sm font-medium disabled:opacity-50"
              >
                {deleting ? "Deleting…" : "Delete Post"}
              </button>
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>,
    document.body
  );
}
