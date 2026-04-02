"use client";

import React, { useState, useRef, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import Image from "next/image";

interface ProfileHeaderProps {
  username: string;
  bio: string;
  profilePictureUrl: string;
  followerCount: number;
  followingCount: number;
  onFollowersClick: () => void;
  onFollowingClick: () => void;
  // Other-user view props
  isOwnProfile?: boolean;
  isFollowing?: boolean;
  isBlocked?: boolean;
  onFollow?: () => Promise<void>;
  onBlock?: () => Promise<void>;
  onReport?: () => void;
}

export const ProfileHeader = ({
  username,
  bio,
  profilePictureUrl,
  followerCount,
  followingCount,
  onFollowersClick,
  onFollowingClick,
  isOwnProfile = true,
  isFollowing = false,
  isBlocked = false,
  onFollow,
  onBlock,
  onReport,
}: ProfileHeaderProps) => {
  const [followPending, setFollowPending] = useState(false);
  const [blockPending, setBlockPending] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const settingsRef = useRef<HTMLDivElement>(null);

  // Close settings dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (
        settingsRef.current &&
        !settingsRef.current.contains(e.target as Node)
      ) {
        setSettingsOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleFollow = async () => {
    if (!onFollow || followPending) return;
    setFollowPending(true);
    try {
      await onFollow();
    } finally {
      setFollowPending(false);
    }
  };

  const handleBlock = async () => {
    if (!onBlock || blockPending) return;
    setSettingsOpen(false);
    setBlockPending(true);
    try {
      await onBlock();
    } finally {
      setBlockPending(false);
    }
  };

  const handleReport = () => {
    setSettingsOpen(false);
    onReport?.();
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.6 }}
      className="glass-card rounded-3xl p-8 md:p-12 mb-12"
    >
      <div className="flex flex-col md:flex-row gap-8 items-start md:items-center">
        {/* Profile Picture */}
        <motion.div
          initial={{ scale: 0.9, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{ delay: 0.2, duration: 0.5 }}
          className="relative w-32 h-32 md:w-48 md:h-48 rounded-full overflow-hidden border-2 border-brand/30 shrink-0 shadow-xl"
        >
          <Image
            src={profilePictureUrl}
            alt={username}
            fill
            className="object-cover"
          />
          <div className="absolute inset-0 bg-linear-to-t from-black/20 to-transparent" />
        </motion.div>

        {/* Profile Info */}
        <div className="flex-1">
          {/* Username and Bio */}
          <motion.div
            initial={{ opacity: 0, x: -20 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: 0.3, duration: 0.5 }}
            className="mb-8"
          >
            <div className="flex flex-wrap items-start gap-4 mb-3">
              <h1 className="text-4xl md:text-5xl font-serif italic text-white">
                {username}
              </h1>

              {/* Follow / Settings buttons for other-user profile */}
              {!isOwnProfile && (
                <div className="flex items-center gap-2 mt-2">
                  <motion.button
                    whileHover={!isBlocked ? { scale: 1.03 } : {}}
                    whileTap={!isBlocked ? { scale: 0.97 } : {}}
                    onClick={isBlocked ? undefined : handleFollow}
                    disabled={followPending || isBlocked}
                    className={`px-5 py-2 rounded-xl text-sm font-semibold transition-colors ${
                      isBlocked
                        ? "bg-white/5 border border-white/10 text-white/30 cursor-not-allowed"
                        : isFollowing
                          ? "bg-white/10 border border-white/20 text-white hover:bg-red-500/20 hover:border-red-500/30 hover:text-red-400"
                          : "bg-brand/20 border border-brand/30 text-brand hover:bg-brand/30"
                    }`}
                  >
                    {isBlocked
                      ? "User blocked"
                      : followPending
                        ? "..."
                        : isFollowing
                          ? "Unfollow"
                          : "Follow"}
                  </motion.button>

                  {/* Settings dropdown */}
                  <div className="relative" ref={settingsRef}>
                    <motion.button
                      whileHover={{ scale: 1.05 }}
                      whileTap={{ scale: 0.95 }}
                      onClick={() => setSettingsOpen((v) => !v)}
                      className="p-2 rounded-xl bg-white/5 border border-white/10 text-accent hover:text-white hover:border-white/20 transition-colors"
                    >
                      <svg
                        xmlns="http://www.w3.org/2000/svg"
                        width="18"
                        height="18"
                        viewBox="0 0 24 24"
                        fill="currentColor"
                      >
                        <circle cx="5" cy="12" r="2" />
                        <circle cx="12" cy="12" r="2" />
                        <circle cx="19" cy="12" r="2" />
                      </svg>
                    </motion.button>

                    <AnimatePresence>
                      {settingsOpen && (
                        <motion.div
                          initial={{ opacity: 0, scale: 0.95, y: -4 }}
                          animate={{ opacity: 1, scale: 1, y: 0 }}
                          exit={{ opacity: 0, scale: 0.95, y: -4 }}
                          transition={{ duration: 0.12 }}
                          className="absolute right-0 mt-2 w-44 rounded-2xl overflow-hidden z-20 border border-white/20 bg-[#1a1a2e] shadow-xl shadow-black/60"
                        >
                          <button
                            onClick={handleBlock}
                            disabled={blockPending}
                            className="w-full flex items-center gap-3 px-4 py-3 text-sm text-white/70 hover:text-white hover:bg-white/10 transition-colors disabled:opacity-50"
                          >
                            <svg
                              xmlns="http://www.w3.org/2000/svg"
                              width="16"
                              height="16"
                              viewBox="0 0 24 24"
                              fill="none"
                              stroke="currentColor"
                              strokeWidth="2"
                              strokeLinecap="round"
                              strokeLinejoin="round"
                            >
                              <circle cx="12" cy="12" r="10" />
                              <line x1="4.93" y1="4.93" x2="19.07" y2="19.07" />
                            </svg>
                            {blockPending
                              ? "..."
                              : isBlocked
                                ? "Unblock"
                                : "Block"}
                          </button>
                          <div className="border-t border-white/10" />
                          <button
                            onClick={handleReport}
                            className="w-full flex items-center gap-3 px-4 py-3 text-sm text-red-400 hover:text-red-300 hover:bg-red-500/10 transition-colors"
                          >
                            <svg
                              xmlns="http://www.w3.org/2000/svg"
                              width="16"
                              height="16"
                              viewBox="0 0 24 24"
                              fill="none"
                              stroke="currentColor"
                              strokeWidth="2"
                              strokeLinecap="round"
                              strokeLinejoin="round"
                            >
                              <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z" />
                              <line x1="4" y1="22" x2="4" y2="15" />
                            </svg>
                            Report
                          </button>
                        </motion.div>
                      )}
                    </AnimatePresence>
                  </div>
                </div>
              )}
            </div>

            <p className="text-accent text-base md:text-lg max-w-md leading-relaxed">
              {isBlocked ? (
                <span className="italic text-white/30">
                  This user&apos;s profile is hidden because you have blocked
                  them.
                </span>
              ) : (
                bio
              )}
            </p>
          </motion.div>

          {/* Stats */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.4, duration: 0.5 }}
            className="flex gap-6 md:gap-8"
          >
            {/* Followers */}
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={onFollowersClick}
              className="flex flex-col items-start gap-2 p-4 rounded-xl hover:bg-white/5 transition-colors"
            >
              <span className="text-2xl md:text-3xl font-bold text-brand">
                {followerCount.toLocaleString()}
              </span>
              <span className="text-xs md:text-sm text-accent uppercase tracking-wider font-semibold">
                Followers
              </span>
            </motion.button>

            {/* Following */}
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={onFollowingClick}
              className="flex flex-col items-start gap-2 p-4 rounded-xl hover:bg-white/5 transition-colors"
            >
              <span className="text-2xl md:text-3xl font-bold text-brand">
                {followingCount.toLocaleString()}
              </span>
              <span className="text-xs md:text-sm text-accent uppercase tracking-wider font-semibold">
                Following
              </span>
            </motion.button>

            {/* Posts Count */}
            <div className="flex flex-col items-start gap-2 p-4 rounded-xl">
              <span className="text-2xl md:text-3xl font-bold text-brand">
                —
              </span>
              <span className="text-xs md:text-sm text-accent uppercase tracking-wider font-semibold">
                Posts
              </span>
            </div>
          </motion.div>
        </div>
      </div>
    </motion.div>
  );
};
