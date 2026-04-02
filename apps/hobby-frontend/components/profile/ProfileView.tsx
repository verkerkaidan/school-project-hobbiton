"use client";

import React, { useEffect, useState } from "react";
import { ProfileHeader } from "@/components/feed/ProfileHeader";
import { UserPosts } from "@/components/feed/UserPosts";
import { UserListModal } from "@/components/profile/UserListModal";
import { ReportModal } from "@/components/profile/ReportModal";
import {
  getProfile,
  getUserPosts,
  getFollowers,
  getFollowing,
  toggleFollow,
  toggleBlock,
  createReport,
  checkIsFollowing,
  checkIsBlocking,
} from "@/queries/getProfile";
import { CURRENT_USER_ID } from "@/lib/auth";
import { UserProfile, User } from "@/types/profile";

interface ProfileViewProps {
  userId: string;
  isOwnProfile: boolean;
}

export function ProfileView({ userId, isOwnProfile }: ProfileViewProps) {
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [loading, setLoading] = useState(true);

  // Cached follower/following lists — loaded lazily when the modal is opened
  const [followers, setFollowers] = useState<User[]>([]);
  const [targetFollowing, setTargetFollowing] = useState<User[]>([]);
  // The logged-in user's own following list, used to render Follow/Unfollow buttons in modals
  const [currentUserFollowing, setCurrentUserFollowing] = useState<User[]>([]);
  // Guard to avoid re-fetching the current user's following list on every modal open
  const [currentUserFollowingLoaded, setCurrentUserFollowingLoaded] =
    useState(false);

  const [isFollowing, setIsFollowing] = useState(false);
  const [isBlocked, setIsBlocked] = useState(false);
  // Whether the profile being viewed follows the current user back
  const [targetFollowsCurrentUser, setTargetFollowsCurrentUser] =
    useState(false);
  // Optimistic offsets applied on top of the server-returned counts to avoid a refetch
  const [followerCountOffset, setFollowerCountOffset] = useState(0);
  const [followingCountOffset, setFollowingCountOffset] = useState(0);

  const [showFollowersModal, setShowFollowersModal] = useState(false);
  const [showFollowingModal, setShowFollowingModal] = useState(false);
  const [loadingFollowers, setLoadingFollowers] = useState(false);
  const [loadingFollowing, setLoadingFollowing] = useState(false);
  const [showReportModal, setShowReportModal] = useState(false);

  // Re-run whenever the viewed profile changes; resets all derived state so
  // stale data from the previous profile doesn't bleed through
  useEffect(() => {
    setFollowers([]);
    setTargetFollowing([]);
    setCurrentUserFollowing([]);
    setCurrentUserFollowingLoaded(false);
    setFollowerCountOffset(0);
    setFollowingCountOffset(0);
    setIsFollowing(false);
    setIsBlocked(false);
    setTargetFollowsCurrentUser(false);

    const load = async () => {
      setLoading(true);
      try {
        if (isOwnProfile) {
          // Own profile: only need the profile data
          const profileData = await getProfile(userId);
          setProfile(profileData);
        } else {
          // Other profile: fetch profile + relationship status in parallel
          const [profileData, following, blocking, followsBack] =
            await Promise.all([
              getProfile(userId),
              checkIsFollowing(CURRENT_USER_ID, userId),
              checkIsBlocking(CURRENT_USER_ID, userId),
              // Check whether the target user follows the current user (mutual follow indicator)
              checkIsFollowing(userId, CURRENT_USER_ID),
            ]);
          setProfile(profileData);
          setIsFollowing(following);
          setIsBlocked(blocking);
          setTargetFollowsCurrentUser(followsBack);
        }
      } catch (error) {
        console.error("[ProfileView] Failed to load profile:", error);
      } finally {
        setLoading(false);
      }
    };

    load();
  }, [userId, isOwnProfile]);

  // Fetches the current user's following list exactly once per profile view;
  // subsequent calls are no-ops so we don't hammer the API on repeated modal opens
  const ensureCurrentUserFollowing = () =>
    !currentUserFollowingLoaded
      ? getFollowing(CURRENT_USER_ID)
          .then((data) => {
            setCurrentUserFollowing(data);
            // For own-profile views, the target's following list IS the current user's list
            if (isOwnProfile) setTargetFollowing(data);
            setCurrentUserFollowingLoaded(true);
          })
          .catch(console.error)
      : Promise.resolve();

  // Opens the followers modal; fetches followers and the current user's following
  // list in parallel so both are ready when the modal renders
  const handleFollowersClick = async () => {
    setShowFollowersModal(true);
    const fetchFollowers =
      followers.length === 0
        ? getFollowers(userId).then(setFollowers).catch(console.error)
        : Promise.resolve();
    setLoadingFollowers(true);
    await Promise.all([fetchFollowers, ensureCurrentUserFollowing()]);
    setLoadingFollowers(false);
  };

  // Opens the following modal; skips re-fetching the target's following list if
  // it was already loaded or if we're viewing our own profile
  const handleFollowingClick = async () => {
    setShowFollowingModal(true);
    const fetchTargetFollowing =
      !isOwnProfile && targetFollowing.length === 0
        ? getFollowing(userId).then(setTargetFollowing).catch(console.error)
        : Promise.resolve();
    setLoadingFollowing(true);
    await Promise.all([fetchTargetFollowing, ensureCurrentUserFollowing()]);
    setLoadingFollowing(false);
  };

  // Build a Set of IDs for O(1) "is this user followed?" lookups in the modal list
  const currentUserFollowingIds = new Set(
    currentUserFollowing.map((u) => u.id),
  );
  // Apply the local offset so the displayed count stays in sync after follow/unfollow
  // without waiting for a server round-trip
  const followerCount = (profile?.followerCount ?? 0) + followerCountOffset;
  const followingCount =
    (isOwnProfile && targetFollowing.length > 0
      ? targetFollowing.length // use the live list length when available
      : (profile?.followingCount ?? 0)) + followingCountOffset;

  // Toggles follow state and updates the follower count + cached followers list optimistically
  const handleFollow = async () => {
    const nowFollowing = await toggleFollow(CURRENT_USER_ID, userId);
    setIsFollowing(nowFollowing);
    setFollowerCountOffset((prev) => prev + (nowFollowing ? 1 : -1));
    if (followers.length > 0) {
      if (nowFollowing) {
        // Add current user to the cached followers list if not already present
        setFollowers((prev) =>
          prev.some((u) => u.id === CURRENT_USER_ID)
            ? prev
            : [
                ...prev,
                {
                  id: CURRENT_USER_ID,
                  username: "",
                  profilePictureUrl: "/jpeg.png",
                },
              ],
        );
      } else {
        setFollowers((prev) => prev.filter((u) => u.id !== CURRENT_USER_ID));
      }
    }
  };

  // Toggles block state; blocking automatically removes any mutual follow relationship
  const handleBlock = async () => {
    const nowBlocking = await toggleBlock(CURRENT_USER_ID, userId);
    setIsBlocked(nowBlocking);
    if (nowBlocking) {
      // Clean up follow state so counts and UI don't show stale "following" indicators
      if (isFollowing) {
        setIsFollowing(false);
        setFollowerCountOffset((prev) => prev - 1);
        setFollowers((prev) => prev.filter((u) => u.id !== CURRENT_USER_ID));
      }
      if (targetFollowsCurrentUser) {
        setFollowingCountOffset((prev) => prev - 1);
        setTargetFollowsCurrentUser(false);
      }
    }
  };

  // Handles follow/unfollow for a user inside the followers/following modal;
  // keeps both the current user's following list and the target's following list in sync
  const handleToggleFollowInModal = async (
    targetUserId: string,
    _isCurrentlyFollowing: boolean,
  ): Promise<boolean> => {
    const isNowFollowing = await toggleFollow(CURRENT_USER_ID, targetUserId);
    // Build a state updater function depending on whether we're now following or unfollowing
    const updater = isNowFollowing
      ? (prev: User[]) =>
          prev.some((u) => u.id === targetUserId)
            ? prev
            : [
                ...prev,
                {
                  id: targetUserId,
                  username: "",
                  profilePictureUrl: "/jpeg.png",
                },
              ]
      : (prev: User[]) => prev.filter((u) => u.id !== targetUserId);

    setCurrentUserFollowing(updater);
    // Keep the own-profile "following" list in sync when viewing your own page
    if (isOwnProfile) setTargetFollowing(updater);
    return isNowFollowing;
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-32">
        <div className="text-accent">Loading profile...</div>
      </div>
    );
  }

  if (!profile) {
    return (
      <div className="flex items-center justify-center py-32">
        <div className="text-accent">Profile not found</div>
      </div>
    );
  }

  return (
    <>
      <ProfileHeader
        username={profile.username}
        bio={profile.bio}
        profilePictureUrl={profile.profilePictureUrl}
        followerCount={followerCount}
        followingCount={followingCount}
        onFollowersClick={handleFollowersClick}
        onFollowingClick={handleFollowingClick}
        isOwnProfile={isOwnProfile}
        isFollowing={isFollowing}
        isBlocked={isBlocked}
        onFollow={handleFollow}
        onBlock={handleBlock}
        onReport={() => setShowReportModal(true)}
      />

      <div className="mb-12">
        <h2 className="text-3xl md:text-4xl font-serif italic text-white mb-8">
          Latest <span className="text-brand">Posts</span>
        </h2>
        {isBlocked ? (
          <div className="glass-card rounded-3xl p-12 flex flex-col items-center justify-center gap-4 text-center">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              width="40"
              height="40"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.5"
              strokeLinecap="round"
              strokeLinejoin="round"
              className="text-white/20"
            >
              <circle cx="12" cy="12" r="10" />
              <line x1="4.93" y1="4.93" x2="19.07" y2="19.07" />
            </svg>
            <p className="text-accent">
              This user&apos;s posts are hidden because you have blocked them.
            </p>
          </div>
        ) : (
          <UserPosts userId={userId} getPosts={getUserPosts} />
        )}
      </div>

      <UserListModal
        isOpen={showFollowersModal}
        onClose={() => setShowFollowersModal(false)}
        title="Followers"
        users={followers}
        isLoading={loadingFollowers}
        currentUserId={CURRENT_USER_ID}
        followingIds={currentUserFollowingIds}
        onToggleFollow={handleToggleFollowInModal}
      />

      <UserListModal
        isOpen={showFollowingModal}
        onClose={() => setShowFollowingModal(false)}
        title="Following"
        users={targetFollowing}
        isLoading={loadingFollowing}
        currentUserId={CURRENT_USER_ID}
        followingIds={currentUserFollowingIds}
        onToggleFollow={handleToggleFollowInModal}
      />

      {!isOwnProfile && (
        <ReportModal
          isOpen={showReportModal}
          targetUsername={profile.username}
          onConfirm={async (reason, description) => {
            await createReport(CURRENT_USER_ID, userId, reason, description);
          }}
          onClose={() => setShowReportModal(false)}
        />
      )}
    </>
  );
}
