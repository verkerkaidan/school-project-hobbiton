"use client";

import React, { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import Image from "next/image";
import Link from "next/link";
import {MediaSet} from "@/types/posts";
import {useProfilePic} from "@/components/ui/ProfilePicProvider";

interface User {
  id: string;
  username: string;
  profilePicture?: MediaSet;
}

interface UserListModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  users: User[];
  isLoading?: boolean;
  currentUserId?: string;
  followingIds?: Set<string>;
  onToggleFollow?: (
    targetUserId: string,
    isCurrentlyFollowing: boolean,
  ) => Promise<boolean>;
}

export const UserListModal = ({
  isOpen,
  onClose,
  title,
  users,
  isLoading = false,
  currentUserId,
  followingIds,
  onToggleFollow,
}: UserListModalProps) => {
  // Local optimistic follow state: map of userId -> isFollowing
  // Kept separately from the parent's followingIds so we can update instantly on click
  const [localFollowing, setLocalFollowing] = useState<Map<string, boolean>>(
    new Map(),
  );
  // Tracks which user IDs have an in-flight toggle request to disable their button
  const [pendingIds, setPendingIds] = useState<Set<string>>(new Set());
  const {imageSrc: contextImageSrc} = useProfilePic();

  // Re-sync local follow state whenever the parent's followingIds or the user list changes
  // (e.g. opening the modal with a different set of users)
  useEffect(() => {
    if (!followingIds) return;
    const map = new Map<string, boolean>();
    users.forEach((u) => {
      map.set(u.id, followingIds.has(u.id));
    });
    setLocalFollowing(map);
  }, [followingIds, users]);

  // Prefer the local optimistic state; fall back to the parent set if not yet initialised
  const isFollowingUser = (userId: string): boolean =>
    localFollowing.get(userId) ?? followingIds?.has(userId) ?? false;

  function getProfilePicture(user: User): string {
    return contextImageSrc
        ?? (user.profilePicture?.fileData
            ? `data:${user.profilePicture.contentType};base64,${user.profilePicture.fileData}`
            : "/jpeg.png");
  }


  const handleToggle = async (user: User) => {
    if (!onToggleFollow || !currentUserId || pendingIds.has(user.id)) return;
    const wasFollowing = isFollowingUser(user.id);

    // Optimistic update: flip state immediately for instant UI feedback
    setLocalFollowing((prev) => new Map(prev).set(user.id, !wasFollowing));
    setPendingIds((prev) => new Set(prev).add(user.id));

    try {
      await onToggleFollow(user.id, wasFollowing);
    } catch {
      // Revert on failure so the UI stays consistent with the server
      setLocalFollowing((prev) => new Map(prev).set(user.id, wasFollowing));
    } finally {
      setPendingIds((prev) => {
        const next = new Set(prev);
        next.delete(user.id);
        return next;
      });
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
            onClick={onClose}
            className="fixed inset-0 bg-black/50 backdrop-blur-sm z-40"
          />

          {/* Modal */}
          <motion.div
            initial={{ opacity: 0, scale: 0.95, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 20 }}
            className="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-full max-w-md mx-auto z-50"
          >
            <div className="glass-card rounded-3xl overflow-hidden">
              {/* Header */}
              <div className="flex items-center justify-between p-6 border-b border-white/5">
                <h2 className="text-xl font-semibold text-white">{title}</h2>
                <button
                  onClick={onClose}
                  className="p-2 hover:bg-white/10 rounded-lg transition-colors"
                >
                  <svg
                    className="w-5 h-5 text-accent hover:text-white"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      strokeLinecap="round"
                      strokeLinejoin="round"
                      strokeWidth={2}
                      d="M6 18L18 6M6 6l12 12"
                    />
                  </svg>
                </button>
              </div>

              {/* Content */}
              <div className="max-h-96 overflow-y-auto">
                {isLoading ? (
                  <div className="p-6 text-center text-accent">Loading...</div>
                ) : users.length === 0 ? (
                  <div className="p-6 text-center text-accent">
                    No users found
                  </div>
                ) : (
                  <div className="divide-y divide-white/5">
                    {users.map((user) => (
                      <motion.div
                        key={user.id}
                        initial={{ opacity: 0, x: -10 }}
                        animate={{ opacity: 1, x: 0 }}
                        className="p-4 flex items-center gap-3 hover:bg-white/5 transition-colors"
                      >
                        <Link
                          href={`/profile/${user.username}`}
                          onClick={onClose}
                          className="relative w-10 h-10 rounded-full overflow-hidden border border-brand/20 shrink-0 hover:border-brand/60 transition-colors"
                        >
                          <Image
                            src={getProfilePicture(user)}
                            alt={user.username}
                            fill
                            className="object-cover"
                          />
                        </Link>
                        <Link
                          href={`/profile/${user.username}`}
                          onClick={onClose}
                          className="text-sm font-medium text-white flex-1 hover:text-brand transition-colors"
                        >
                          {user.username}
                        </Link>
                        {onToggleFollow &&
                          currentUserId &&
                          user.id !== currentUserId && (
                            <button
                              onClick={() => handleToggle(user)}
                              disabled={pendingIds.has(user.id)}
                              className={`shrink-0 px-3 py-1 rounded-full text-xs font-medium transition-colors disabled:opacity-50 ${
                                isFollowingUser(user.id)
                                  ? "bg-white/10 text-white hover:bg-red-500/20 hover:text-red-400"
                                  : "bg-brand/20 text-brand hover:bg-brand/30"
                              }`}
                            >
                              {pendingIds.has(user.id)
                                ? "..."
                                : isFollowingUser(user.id)
                                  ? "Unfollow"
                                  : "Follow"}
                            </button>
                          )}
                      </motion.div>
                    ))}
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
