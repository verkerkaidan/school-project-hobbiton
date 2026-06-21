"use client";

import React, { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
    X,
    Trash2,
    AlertTriangle,
} from "lucide-react";
import {useErrorToast} from "@/components/ui/ErrorToastProvider";
import {deleteUserProfile} from "@/lib/adminClient";

interface DeleteUserConfirmModalProps {
    isOpen: boolean;
    targetUserId: string;
    targetUsername: string;
    onClose: () => void;
    onActionComplete: () => void;
}

export const DeleteUserConfirmModal = ({
   isOpen,
   targetUserId,
   targetUsername,
   onClose,
   onActionComplete,
}: DeleteUserConfirmModalProps) => {
    const { showErrorToast } = useErrorToast();
    const [deleting, setDeleting] = useState(false);

    const handleConfirm = async () => {
        setDeleting(true);
        try {
            await deleteUserProfile(targetUserId);
            showErrorToast("Account was deleted", "", "success");
            onActionComplete();
        } catch (error) {
            const message = error instanceof Error ? error.message : "";
            showErrorToast("Account deletion failed", message, "error");
        } finally {
            setDeleting(false);
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
                        className="fixed inset-0 bg-black/60 backdrop-blur-sm z-50"
                        onClick={() => !deleting && onClose()}
                    />

                    {/* ✅ Centered wrapper — same pattern as DeletePostsConfirmModal */}
                    <motion.div
                        initial={{ opacity: 0, scale: 0.95, y: 10 }}
                        animate={{ opacity: 1, scale: 1, y: 0 }}
                        exit={{ opacity: 0, scale: 0.95, y: 10 }}
                        transition={{ type: "spring", stiffness: 300, damping: 25 }}
                        className="fixed inset-0 z-50 flex items-center justify-center p-4 pointer-events-none"
                    >
                        {/* ✅ Modal panel card */}
                        <div
                            className="relative w-full max-w-md bg-background-secondary border border-accent/15 rounded-2xl shadow-2xl pointer-events-auto"
                            onClick={(e) => e.stopPropagation()}
                        >
                            {/* Header */}
                            <div className="flex items-center justify-between px-6 py-5 border-b border-accent/15">
                                <div className="flex items-center gap-3">
                                    <div className="w-8 h-8 rounded-lg bg-rose-500/15 border border-rose-500/20 flex items-center justify-center">
                                        <Trash2 className="w-4 h-4 text-rose-400" />
                                    </div>
                                    <div>
                                        <h2 className="text-accent font-semibold text-sm">Delete Account</h2>
                                        <p className="text-accent/60 text-xs">@{targetUsername}</p>
                                    </div>
                                </div>
                                <button
                                    onClick={onClose}
                                    disabled={deleting}
                                    className="w-8 h-8 flex items-center justify-center rounded-lg text-accent/60 hover:text-accent hover:bg-accent/10 transition-colors disabled:opacity-40"
                                >
                                    <X className="w-4 h-4" />
                                </button>
                            </div>

                            {/* Body */}
                            <div className="p-6 flex flex-col items-center text-center gap-4">
                                <div className="w-14 h-14 rounded-2xl bg-rose-500/10 border border-rose-500/20 flex items-center justify-center">
                                    <AlertTriangle className="w-6 h-6 text-rose-400" />
                                </div>
                                <div>
                                    <h3 className="text-accent font-semibold text-base">Confirm deletion</h3>
                                    <p className="text-accent/70 text-sm mt-1">
                                        You are about to permanently delete{" "}
                                        <span className="text-accent/85">@{targetUsername}</span>'s account.
                                        This action <span className="text-rose-400">cannot be undone</span>.
                                    </p>
                                </div>
                            </div>

                            {/* Footer */}
                            <div className="px-6 py-5 border-t border-accent/15">
                                <div className="flex gap-3">
                                    <button
                                        onClick={onClose}
                                        disabled={deleting}
                                        className="flex-1 px-4 py-2.5 rounded-xl border border-accent/20 text-accent/70 hover:text-accent hover:border-accent/35 transition-colors text-sm font-medium disabled:opacity-40"
                                    >
                                        Cancel
                                    </button>
                                    <button
                                        onClick={handleConfirm}
                                        disabled={deleting}
                                        className="flex-1 px-4 py-2.5 rounded-xl bg-rose-500/20 border border-rose-500/30 text-rose-400 hover:bg-rose-500/30 hover:text-rose-300 transition-colors text-sm font-medium disabled:opacity-50"
                                    >
                                        {deleting ? "Deleting…" : "Confirm Deletion"}
                                    </button>
                                </div>
                            </div>
                        </div>
                    </motion.div>
                </>
            )}
        </AnimatePresence>
    );
};