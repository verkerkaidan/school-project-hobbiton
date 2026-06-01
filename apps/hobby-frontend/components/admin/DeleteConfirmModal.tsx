"use client";

import React, { useEffect, useState, useCallback } from "react";
import { motion, AnimatePresence } from "framer-motion";
import {
  X,
  Trash2,
  AlertTriangle,
  FileText,
  Dumbbell,
  BookOpen,
} from "lucide-react";
import { getUserPosts } from "@/queries/getProfile";
import { deletePost, deleteAllUserPosts } from "@/lib/adminClient";
import type { Post } from "@/types/posts";

interface DeleteConfirmModalProps {
  isOpen: boolean;
  targetUserId: string;
  targetUsername: string;
  onClose: () => void;
  onActionComplete?: () => void;
}

type Stage = "list" | "confirm";

export const DeleteConfirmModal = ({
  isOpen,
  targetUserId,
  targetUsername,
  onClose,
  onActionComplete,
}: DeleteConfirmModalProps) => {
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(false);
  const [stage, setStage] = useState<Stage>("list");
  /** null = delete all; string = delete single post id */
  const [pendingDeleteId, setPendingDeleteId] = useState<string | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const fetchPosts = useCallback(async () => {
    if (!targetUserId) return;
    setLoading(true);
    setError(null);
    try {
      const fetched = await getUserPosts(targetUserId);
      setPosts(fetched);
    } catch {
      setError("Failed to load posts.");
    } finally {
      setLoading(false);
    }
  }, [targetUserId]);

  useEffect(() => {
    if (isOpen) {
      setStage("list");
      setPendingDeleteId(null);
      setError(null);
      fetchPosts();
    }
  }, [isOpen, fetchPosts]);

  const openConfirm = (postId: string | null) => {
    setPendingDeleteId(postId);
    setStage("confirm");
  };

  const handleConfirm = async () => {
    setDeleting(true);
    setError(null);
    try {
      if (pendingDeleteId === null) {
        await deleteAllUserPosts(targetUserId);
        setPosts([]);
      } else {
        await deletePost(pendingDeleteId);
        setPosts((prev) => prev.filter((p) => p.id !== pendingDeleteId));
      }
      setStage("list");
      onActionComplete?.();
    } catch {
      setError("Deletion failed. Please try again.");
      setStage("confirm");
    } finally {
      setDeleting(false);
    }
  };

  const confirmCount = pendingDeleteId === null ? posts.length : 1;
  const confirmLabel =
    pendingDeleteId === null
      ? `all ${posts.length} post${posts.length !== 1 ? "s" : ""} by @${targetUsername}`
      : `1 post by @${targetUsername}`;

  const selectedPost = pendingDeleteId
    ? posts.find((p) => p.id === pendingDeleteId)
    : null;

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

          {/* Modal */}
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 10 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 10 }}
            transition={{ type: "spring", stiffness: 300, damping: 25 }}
            className="fixed inset-0 z-50 flex items-center justify-center p-4 pointer-events-none"
          >
            <div
              className="relative w-full max-w-lg bg-[#0d0d0f] border border-white/10 rounded-2xl shadow-2xl pointer-events-auto flex flex-col max-h-[80vh]"
              onClick={(e) => e.stopPropagation()}
            >
              {/* Header */}
              <div className="flex items-center justify-between px-6 py-5 border-b border-white/5 shrink-0">
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 rounded-lg bg-rose-500/15 border border-rose-500/20 flex items-center justify-center">
                    <Trash2 className="w-4 h-4 text-rose-400" />
                  </div>
                  <div>
                    <h2 className="text-white font-semibold text-sm">
                      Delete Posts
                    </h2>
                    <p className="text-white/40 text-xs">@{targetUsername}</p>
                  </div>
                </div>
                <button
                  onClick={onClose}
                  disabled={deleting}
                  className="w-8 h-8 flex items-center justify-center rounded-lg text-white/40 hover:text-white hover:bg-white/5 transition-colors disabled:opacity-40"
                >
                  <X className="w-4 h-4" />
                </button>
              </div>

              {/* Body */}
              <div className="flex-1 overflow-y-auto">
                <AnimatePresence mode="wait">
                  {/* ─── Stage: List ─── */}
                  {stage === "list" && (
                    <motion.div
                      key="list"
                      initial={{ opacity: 0, x: -10 }}
                      animate={{ opacity: 1, x: 0 }}
                      exit={{ opacity: 0, x: -10 }}
                      className="p-6 space-y-3"
                    >
                      {loading ? (
                        <div className="flex items-center justify-center py-10 text-white/30 text-sm">
                          Loading posts…
                        </div>
                      ) : posts.length === 0 ? (
                        <div className="flex flex-col items-center justify-center py-10 gap-2 text-white/30">
                          <FileText className="w-8 h-8 opacity-40" />
                          <span className="text-sm">No posts found.</span>
                        </div>
                      ) : (
                        <>
                          {posts.map((post) => (
                            <div
                              key={post.id}
                              className="flex items-center gap-3 p-3 rounded-xl bg-white/3 border border-white/5 hover:border-white/10 transition-colors group"
                            >
                              <div className="w-7 h-7 rounded-md bg-white/5 flex items-center justify-center shrink-0">
                                {post.hobbyType === "GYM" ? (
                                  <Dumbbell className="w-3.5 h-3.5 text-white/40" />
                                ) : (
                                  <BookOpen className="w-3.5 h-3.5 text-white/40" />
                                )}
                              </div>
                              <div className="flex-1 min-w-0">
                                <p className="text-white text-sm font-medium truncate">
                                  {post.title}
                                </p>
                                <p className="text-white/30 text-xs">
                                  {post.hobbyType} ·{" "}
                                  {post.createdAt
                                    ? new Date(
                                        post.createdAt,
                                      ).toLocaleDateString()
                                    : "—"}
                                </p>
                              </div>
                              <button
                                onClick={() => openConfirm(post.id)}
                                className="opacity-0 group-hover:opacity-100 w-7 h-7 flex items-center justify-center rounded-lg text-rose-400 hover:bg-rose-500/10 transition-all shrink-0"
                                title="Delete this post"
                              >
                                <Trash2 className="w-3.5 h-3.5" />
                              </button>
                            </div>
                          ))}
                        </>
                      )}

                      {error && (
                        <p className="text-rose-400 text-xs text-center pt-1">
                          {error}
                        </p>
                      )}
                    </motion.div>
                  )}

                  {/* ─── Stage: Confirm ─── */}
                  {stage === "confirm" && (
                    <motion.div
                      key="confirm"
                      initial={{ opacity: 0, x: 10 }}
                      animate={{ opacity: 1, x: 0 }}
                      exit={{ opacity: 0, x: 10 }}
                      className="p-6 flex flex-col items-center text-center gap-4"
                    >
                      <div className="w-14 h-14 rounded-2xl bg-rose-500/10 border border-rose-500/20 flex items-center justify-center mt-2">
                        <AlertTriangle className="w-6 h-6 text-rose-400" />
                      </div>
                      <div>
                        <h3 className="text-white font-semibold text-base">
                          Confirm deletion of {confirmCount}{" "}
                          {confirmCount === 1 ? "post" : "posts"}
                        </h3>
                        <p className="text-white/40 text-sm mt-1">
                          You are about to permanently delete{" "}
                          <span className="text-white/70">{confirmLabel}</span>.
                          This action{" "}
                          <span className="text-rose-400">
                            cannot be undone
                          </span>
                          .
                        </p>
                        {selectedPost && (
                          <p className="mt-3 text-white/30 text-xs italic">
                            "{selectedPost.title}"
                          </p>
                        )}
                      </div>

                      {error && (
                        <p className="text-rose-400 text-xs">{error}</p>
                      )}
                    </motion.div>
                  )}
                </AnimatePresence>
              </div>

              {/* Footer */}
              <div className="px-6 py-5 border-t border-white/5 shrink-0">
                {stage === "list" ? (
                  <div className="flex gap-3">
                    <button
                      onClick={onClose}
                      className="flex-1 px-4 py-2.5 rounded-xl border border-white/10 text-white/50 hover:text-white hover:border-white/20 transition-colors text-sm font-medium"
                    >
                      Cancel
                    </button>
                    <button
                      onClick={() => openConfirm(null)}
                      disabled={posts.length === 0 || loading}
                      className="flex-1 px-4 py-2.5 rounded-xl bg-rose-500/15 border border-rose-500/25 text-rose-400 hover:bg-rose-500/25 hover:text-rose-300 transition-colors text-sm font-medium disabled:opacity-30 disabled:cursor-not-allowed"
                    >
                      Delete All ({posts.length})
                    </button>
                  </div>
                ) : (
                  <div className="flex gap-3">
                    <button
                      onClick={() => {
                        setStage("list");
                        setError(null);
                      }}
                      disabled={deleting}
                      className="flex-1 px-4 py-2.5 rounded-xl border border-white/10 text-white/50 hover:text-white hover:border-white/20 transition-colors text-sm font-medium disabled:opacity-40"
                    >
                      Back
                    </button>
                    <button
                      onClick={handleConfirm}
                      disabled={deleting}
                      className="flex-1 px-4 py-2.5 rounded-xl bg-rose-500/20 border border-rose-500/30 text-rose-400 hover:bg-rose-500/30 hover:text-rose-300 transition-colors text-sm font-medium disabled:opacity-50"
                    >
                      {deleting ? "Deleting…" : `Confirm Deletion`}
                    </button>
                  </div>
                )}
              </div>
            </div>
          </motion.div>
        </>
      )}
    </AnimatePresence>
  );
};
