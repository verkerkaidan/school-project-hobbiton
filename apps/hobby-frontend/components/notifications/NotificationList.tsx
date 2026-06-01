"use client";

import React from "react";
import Link from "next/link";
<<<<<<< HEAD
import { Notification } from "@/types/notifications";
import { markNotificationRead } from "@/lib/notificationClient";
import { Bell, UserPlus } from "lucide-react";
=======
import { useRouter } from "next/navigation";
import { Notification } from "@/types/notifications";
import { markNotificationRead } from "@/lib/notificationClient";
import { Bell, UserPlus, Heart, MessageCircle } from "lucide-react";
>>>>>>> origin/main

function timeAgo(dateStr: string): string {
  const diff = Date.now() - new Date(dateStr).getTime();
  const seconds = Math.floor(diff / 1000);
  if (seconds < 60) return "just now";
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  if (days < 7) return `${days}d ago`;
  return new Date(dateStr).toLocaleDateString();
}

interface NotificationItemProps {
  notification: Notification;
  onRead: (id: string) => void;
}

function NotificationIcon({ type }: { type: Notification["type"] }) {
  switch (type) {
    case "FOLLOW":
      return <UserPlus className="w-4 h-4 text-brand" />;
<<<<<<< HEAD
=======
    case "LIKE":
      return <Heart className="w-4 h-4 text-brand" />;
    case "COMMENT":
      return <MessageCircle className="w-4 h-4 text-brand" />;
>>>>>>> origin/main
    default:
      return <Bell className="w-4 h-4 text-brand" />;
  }
}

<<<<<<< HEAD
/** Renders a notification message, turning a leading "@username" into a profile link. */
function NotificationMessage({ message }: { message: string }) {
  const match = message.match(/^(@\S+)(.*)/);
  if (!match) return <>{message}</>;
  const [, handle, rest] = match;
  const username = handle.slice(1); // strip the @
=======
/** Renders a notification message with smart linking.
 *  - A leading @username is always linked to the author's profile.
 *  - For USER_POST notifications the quoted post title (e.g. "My Title") is
 *    also linked to the author's profile until a dedicated post page exists.
 */
function NotificationMessage({ notification }: { notification: Notification }) {
  const { message, type, referenceId } = notification;
  const atMatch = message.match(/^(@\S+)(.*)/);
  if (!atMatch) return <>{message}</>;

  const [, handle, rest] = atMatch;
  const username = handle.slice(1); // strip the @

  // For USER_POST with a referenceId, make the quoted title a second link
  if (type === "USER_POST" && referenceId) {
    const titleMatch = rest.match(/^( published ")(.*?)(")/);
    if (titleMatch) {
      const [, prefix, title, suffix] = titleMatch;
      const trailing = rest.slice(prefix.length + title.length + suffix.length);
      return (
        <>
          <Link
            href={`/profile/${username}`}
            className="text-brand hover:underline"
            onClick={(e) => e.stopPropagation()}
          >
            {handle}
          </Link>
          {prefix}
          <Link
            href={`/posts/${referenceId}`}
            className="text-brand/80 hover:underline"
            onClick={(e) => e.stopPropagation()}
          >
            {title}
          </Link>
          {suffix}
          {trailing}
        </>
      );
    }
  }

>>>>>>> origin/main
  return (
    <>
      <Link
        href={`/profile/${username}`}
        className="text-brand hover:underline"
        onClick={(e) => e.stopPropagation()}
      >
        {handle}
      </Link>
      {rest}
    </>
  );
}

export function NotificationItem({
  notification,
  onRead,
}: NotificationItemProps) {
  const isUnread = !notification.readAt;
<<<<<<< HEAD
=======
  const router = useRouter();
  const commentPreview =
    notification.type === "COMMENT" && notification.content
      ? notification.content.length > 60
        ? notification.content.slice(0, 60) + "…"
        : notification.content
      : null;

  const postHref =
    (notification.type === "COMMENT" || notification.type === "LIKE") &&
    notification.referenceId
      ? `/posts/${notification.referenceId}`
      : null;
>>>>>>> origin/main

  const handleClick = async () => {
    if (isUnread) {
      try {
        await markNotificationRead(notification.id);
        onRead(notification.id);
      } catch {
        // silently ignore
      }
    }
<<<<<<< HEAD
  };

  return (
    <button
      onClick={handleClick}
      className={`w-full text-left flex items-start gap-4 p-4 rounded-xl border transition-all duration-200
=======
    if (postHref) {
      router.push(postHref);
    }
  };

  const sharedClassName = `w-full text-left flex items-start gap-4 p-4 rounded-xl border transition-all duration-200
>>>>>>> origin/main
                ${
                  isUnread
                    ? "bg-brand-muted/40 border-brand/20 hover:bg-brand-muted/60"
                    : "bg-white/5 border-white/5 hover:bg-white/10"
<<<<<<< HEAD
                }`}
    >
=======
                }`;

  const inner = (
    <>
>>>>>>> origin/main
      <div
        className={`shrink-0 mt-1 p-2 rounded-full ${isUnread ? "bg-brand/20" : "bg-white/10"}`}
      >
        <NotificationIcon type={notification.type} />
      </div>
      <div className="flex-1 min-w-0">
        <p
          className={`text-sm ${isUnread ? "text-white font-medium" : "text-accent"}`}
        >
<<<<<<< HEAD
          <NotificationMessage message={notification.message} />
        </p>
=======
          <NotificationMessage notification={notification} />
        </p>
        {commentPreview && (
          <p className="text-xs text-accent/70 mt-0.5 italic">
            &ldquo;{commentPreview}&rdquo;
          </p>
        )}
>>>>>>> origin/main
        <p className="text-xs text-accent/60 mt-1">
          {timeAgo(notification.createdAt)}
        </p>
      </div>
      {isUnread && (
        <span
          className="shrink-0 mt-2 w-2 h-2 rounded-full bg-brand"
          aria-label="Unread"
        />
      )}
<<<<<<< HEAD
=======
    </>
  );

  if (postHref) {
    return (
      <button onClick={handleClick} className={sharedClassName}>
        {inner}
      </button>
    );
  }

  return (
    <button onClick={handleClick} className={sharedClassName}>
      {inner}
>>>>>>> origin/main
    </button>
  );
}

interface NotificationListProps {
  notifications: Notification[];
  onNotificationRead: (id: string) => void;
}

export function NotificationList({
  notifications,
  onNotificationRead,
}: NotificationListProps) {
  if (notifications.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-16 text-accent">
        <Bell className="w-12 h-12 mb-4 opacity-30" />
        <p className="text-sm">No notifications yet</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col gap-2">
      {notifications.map((n) => (
        <NotificationItem
          key={n.id}
          notification={n}
          onRead={onNotificationRead}
        />
      ))}
    </div>
  );
}
