"use client";

import React, { useState, useRef, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Bell, BellOff } from "lucide-react";
import Image from "next/image";
import { EditProfilePictureModal } from "@/components/profile/EditProfilePictureModal";
import {MediaSet} from "@/types/posts";
import {postChangeProfilePicture} from "@/lib/profileClient";
import Cookies from "js-cookie";
import {useProfilePic} from "@/components/ui/ProfilePicProvider";

interface ProfileHeaderProps {
  username: string;
  bio: string;
  profilePicture?: MediaSet;
  followerCount: number;
  followingCount: number;
  postCount?: number;
  onFollowersClick: () => void;
  onFollowingClick: () => void;
  // Other-user view props
  isOwnProfile?: boolean;
  isFollowing?: boolean;
  isBlocked?: boolean;
  isSubscribed?: boolean;
  showPreferenceWarning?: boolean;
  onFollow?: () => Promise<void>;
  onBlock?: () => Promise<void>;
  onSubscriptionToggle?: () => Promise<void>;
  onReport?: () => void;
}

export const ProfileHeader = ({
  username,
  bio,
  profilePicture,
  followerCount,
  followingCount,
  postCount,
  onFollowersClick,
  onFollowingClick,
  isOwnProfile = true,
  isFollowing = false,
  isBlocked = false,
  isSubscribed = false,
  showPreferenceWarning = false,
  onFollow,
  onBlock,
  onSubscriptionToggle,
  onReport,
}: ProfileHeaderProps) => {
  const [editPictureOpen, setEditPictureOpen] = useState(false);
  const [followPending, setFollowPending] = useState(false);
  const [blockPending, setBlockPending] = useState(false);
  const [subscriptionPending, setSubscriptionPending] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const settingsRef = useRef<HTMLDivElement>(null);
  const { imageSrc: contextImageSrc, setImageSrc, showToast } = useProfilePic();

  function getImageSrc() {
    if (isOwnProfile) {
      return contextImageSrc
          ?? (profilePicture?.fileData
              ? `data:${profilePicture.contentType};base64,${profilePicture.fileData}`
              : "/jpeg.png");
    }
    else {
      return profilePicture?.fileData
          ? `data:${profilePicture.contentType};base64,${profilePicture.fileData}`
          : "/jpeg.png";
    }
  }

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

  const handleSubscriptionToggle = async () => {
    if (!onSubscriptionToggle || subscriptionPending) return;
    setSubscriptionPending(true);
    try {
      await onSubscriptionToggle();
    } finally {
      setSubscriptionPending(false);
    }
  };

  const handleReport = () => {
    setSettingsOpen(false);
    onReport?.();
  };

  async function onEditProfilePicture(newFile: File) {
    const userId = Cookies.get("userId") ?? "";
    const token = Cookies.get("token") ?? "";

    if (contextImageSrc?.startsWith("blob:")) {
      URL.revokeObjectURL(contextImageSrc);
    }

    const previewUrl = URL.createObjectURL(newFile);
    setImageSrc(previewUrl); // optimistic update

    try {
      const oldId = profilePicture?.relevantId ?? "";
      await postChangeProfilePicture(userId, [newFile], token, oldId);
      showToast("Profile picture updated", "", "success");
    } catch (error) {
      setImageSrc(undefined); // revert
      const message = error instanceof Error ? error.message : "";
      showToast("Failed to update profile picture", message, "error");
    }
  }

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
          className="relative w-32 h-32 md:w-48 md:h-48 shrink-0"
        >
          <div className="relative w-full h-full rounded-full overflow-hidden border-2 border-brand/30 shadow-xl">
            <Image
              src={getImageSrc()}
              alt={username}
              fill
              className="object-cover"
            />
            <div className="absolute inset-0 bg-linear-to-t from-black/20 to-transparent" />
          </div>

          {isOwnProfile && (
            <motion.button
              whileHover={{ scale: 1.1 }}
              whileTap={{ scale: 0.9 }}
              onClick={() => setEditPictureOpen(true)}
              title="Edit profile picture"
              className="absolute bottom-1 right-1 md:bottom-2 md:right-2 w-8 h-8 md:w-9 md:h-9 rounded-full
                         bg-[#0f0f1a] border border-white/20 flex items-center justify-center
                         text-accent hover:text-brand-light hover:border-brand/40 transition-colors shadow-lg"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="14"
                height="14"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
              >
                <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
              </svg>
            </motion.button>
          )}
        </motion.div>

        {isOwnProfile && (
          <EditProfilePictureModal
            isOpen={editPictureOpen}
            currentImage={getImageSrc()}
            username={username}
            onClose={() => setEditPictureOpen(false)}
            onSave={onEditProfilePicture}
          />
        )}

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
                <div className="flex flex-col gap-1 mt-2">
                  <div className="flex items-center gap-2">
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

                    {/* Post notification bell — shown only while following */}
                    {isFollowing && !isBlocked && (
                      <motion.button
                        whileHover={{ scale: 1.05 }}
                        whileTap={{ scale: 0.95 }}
                        onClick={handleSubscriptionToggle}
                        disabled={subscriptionPending}
                        title={
                          isSubscribed
                            ? "Turn off post notifications"
                            : "Turn on post notifications"
                        }
                        className={`p-2 rounded-xl border transition-colors ${
                          isSubscribed
                            ? "bg-brand/20 border-brand/30 text-brand hover:bg-brand/10"
                            : "bg-white/5 border-white/10 text-accent hover:text-white hover:border-white/20"
                        } disabled:opacity-50`}
                      >
                        {isSubscribed ? (
                          <Bell size={16} />
                        ) : (
                          <BellOff size={16} />
                        )}
                      </motion.button>
                    )}

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
                                <line
                                  x1="4.93"
                                  y1="4.93"
                                  x2="19.07"
                                  y2="19.07"
                                />
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

                  {/* Global preference warning */}
                  {showPreferenceWarning && isSubscribed && isFollowing && (
                    <p className="text-xs text-amber-400">
                      Also enable{" "}
                      <a
                        href="/settings"
                        className="underline hover:text-amber-300"
                        onClick={(e) => e.stopPropagation()}
                      >
                        &ldquo;Posts from followed users&rdquo; in Settings
                      </a>{" "}
                      to receive these notifications.
                    </p>
                  )}
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
              className="flex flex-col items-start gap-2 p-4 rounded-xl transition-colors"
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
              className="flex flex-col items-start gap-2 p-4 rounded-xl transition-colors"
            >
              <span className="text-2xl md:text-3xl font-bold text-brand">
                {followingCount.toLocaleString()}
              </span>
              <span className="text-xs md:text-sm text-accent uppercase tracking-wider font-semibold">
                Following
              </span>
            </motion.button>

            {/* Posts Count */}
            {!isBlocked && (
              <div className="flex flex-col items-start gap-2 p-4 rounded-xl">
                <span className="text-2xl md:text-3xl font-bold text-brand">
                  {postCount === undefined ? "—" : postCount.toLocaleString()}
                </span>
                <span className="text-xs md:text-sm text-accent uppercase tracking-wider font-semibold">
                  Posts
                </span>
              </div>
            )}
          </motion.div>
        </div>
      </div>
    </motion.div>
  );
};
