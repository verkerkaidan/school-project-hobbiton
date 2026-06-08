"use client";

import React, { useEffect, useRef, useState } from "react";
import Image from "next/image";
import Link from "next/link";
import { useCurrentUser } from "@/hooks/useCurrentUser";
import {
  checkIsFollowing,
  checkIsBlocking,
  toggleFollow,
  toggleBlock,
  createReport,
} from "@/queries/getProfile";
import { ReportModal } from "@/components/profile/ReportModal";
import {MediaSet} from "@/types/posts";
import {useProfilePic} from "@/components/ui/ProfilePicProvider";

interface PostAuthorCardProps {
  authorId: string;
  username: string;
  profilePicture?: MediaSet;
}

export function PostAuthorCard({
  authorId,
  username,
  profilePicture,
}: PostAuthorCardProps) {
  const currentUser = useCurrentUser();
  const currentUserId = currentUser?.id ?? "";

  const isOwnPost = currentUserId !== "" && currentUserId === authorId;

  const [isFollowing, setIsFollowing] = useState(false);
  const [isBlocked, setIsBlocked] = useState(false);
  const [followPending, setFollowPending] = useState(false);
  const [blockPending, setBlockPending] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [showReportModal, setShowReportModal] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);
  const {imageSrc: contextImageSrc} = useProfilePic();

  const imageSrc = profilePicture?.fileData
          ? `data:${profilePicture.contentType};base64,${profilePicture.fileData}`
          : "/jpeg.png";

  useEffect(() => {
    if (!currentUserId || isOwnPost) return;
    Promise.all([
      checkIsFollowing(currentUserId, authorId),
      checkIsBlocking(currentUserId, authorId),
    ]).then(([following, blocking]) => {
      setIsFollowing(following);
      setIsBlocked(blocking);
    });
  }, [currentUserId, authorId, isOwnPost]);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setMenuOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleFollow = async () => {
    if (followPending) return;
    setFollowPending(true);
    try {
      const nowFollowing = await toggleFollow(currentUserId, authorId);
      setIsFollowing(nowFollowing);
    } finally {
      setFollowPending(false);
    }
  };

  const handleBlock = async () => {
    if (blockPending) return;
    setMenuOpen(false);
    setBlockPending(true);
    try {
      const nowBlocking = await toggleBlock(currentUserId, authorId);
      setIsBlocked(nowBlocking);
      if (nowBlocking && isFollowing) {
        setIsFollowing(false);
      }
    } finally {
      setBlockPending(false);
    }
  };

  return (
    <>
      <div className="flex items-center justify-between gap-4 py-4 border-b border-white/10 mb-6">
        {/* Author info */}
        <Link
          href={`/profile/${username}`}
          className="flex items-center gap-3 group"
        >
          <div className="relative w-10 h-10 rounded-full overflow-hidden border border-brand/30 shrink-0">
            <Image
              src={imageSrc}
              alt={username}
              fill
              className="object-cover"
            />
          </div>
          <span className="text-sm font-medium group-hover:text-brand-light transition-colors duration-200 light-mode-text">
            {username}
          </span>
        </Link>

        {/* Actions (only for other users' posts) */}
        {!isOwnPost && currentUserId && (
          <div className="flex items-center gap-2">
            {/* Follow button */}
            <button
              onClick={handleFollow}
              disabled={followPending || isBlocked}
              className={`px-4 py-1.5 rounded-full text-xs font-semibold transition-colors duration-200 disabled:opacity-50 ${
                isFollowing
                  ? "bg-white/10 text-accent hover:bg-white/15"
                  : "bg-brand text-white hover:bg-brand-light"
              }`}
            >
              {followPending ? "..." : isFollowing ? "Following" : "Follow"}
            </button>

            {/* 3-dot menu */}
            <div ref={menuRef} className="relative">
              <button
                onClick={() => setMenuOpen((prev) => !prev)}
                className="w-8 h-8 flex items-center justify-center rounded-full bg-white/5 hover:bg-white/10 text-accent transition-colors"
                aria-label="More options"
              >
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="16"
                  height="16"
                  viewBox="0 0 24 24"
                  fill="currentColor"
                >
                  <circle cx="5" cy="12" r="2" />
                  <circle cx="12" cy="12" r="2" />
                  <circle cx="19" cy="12" r="2" />
                </svg>
              </button>

              {menuOpen && (
                <div className="absolute right-0 mt-2 w-44 rounded-xl bg-neutral-900 text-white shadow-lg border border-white/10 backdrop-blur-md p-1.5 z-30 animate-in fade-in zoom-in-95">
                  <button
                    onClick={() => {
                      setMenuOpen(false);
                      setShowReportModal(true);
                    }}
                    className="w-full text-left px-3 py-2 rounded-lg text-sm hover:bg-white/10 transition"
                  >
                    Report
                  </button>
                  <button
                    onClick={handleBlock}
                    disabled={blockPending}
                    className="w-full text-left px-3 py-2 rounded-lg text-sm hover:bg-white/10 transition text-red-400 disabled:opacity-50"
                  >
                    {blockPending ? "..." : isBlocked ? "Unblock" : "Block"}
                  </button>
                </div>
              )}
            </div>
          </div>
        )}
      </div>

      {!isOwnPost && (
        <ReportModal
          isOpen={showReportModal}
          targetUsername={username}
          onConfirm={async (reason, description) => {
            await createReport(currentUserId, authorId, reason, description);
          }}
          onClose={() => setShowReportModal(false)}
        />
      )}
    </>
  );
}
