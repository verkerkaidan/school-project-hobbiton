"use client";

import React, { useCallback, useEffect, useState } from "react";
import { Notification } from "@/types/notifications";
import {
  fetchNotifications,
  markAllNotificationsRead,
} from "@/lib/notificationClient";
import { NotificationList } from "@/components/notifications/NotificationList";
import { useNotifications } from "@/components/notifications/NotificationProvider";
import { CheckCheck, Loader2 } from "lucide-react";

const PAGE_SIZE = 10;

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [total, setTotal] = useState(0);
  const [offset, setOffset] = useState(0);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const { setUnreadCount, subscribe } = useNotifications();

  const loadPage = useCallback(
    async (currentOffset: number, replace: boolean) => {
      try {
        const data = await fetchNotifications(PAGE_SIZE, currentOffset);
        setNotifications((prev) =>
          replace ? data.notifications : [...prev, ...data.notifications],
        );
        setTotal(data.total);
        setOffset(currentOffset + data.notifications.length);
      } catch {
        // silently ignore
      }
    },
    [],
  );

  useEffect(() => {
    setLoading(true);
    loadPage(0, true).finally(() => setLoading(false));
  }, [loadPage]);

  // Subscribe directly to live notifications — no state/effect timing issues
  useEffect(() => {
    return subscribe((notif) => {
      setNotifications((prev) => {
        if (prev.some((n) => n.id === notif.id)) return prev;
        return [notif, ...prev];
      });
      setTotal((t) => t + 1);
    });
  }, [subscribe]);

  const handleLoadMore = async () => {
    setLoadingMore(true);
    await loadPage(offset, false);
    setLoadingMore(false);
  };

  const handleNotificationRead = (id: string) => {
    setNotifications((prev) =>
      prev.map((n) =>
        n.id === id ? { ...n, readAt: new Date().toISOString() } : n,
      ),
    );
    setUnreadCount((c) => Math.max(0, c - 1));
  };

  const handleMarkAllRead = async () => {
    try {
      await markAllNotificationsRead();
      const now = new Date().toISOString();
      setNotifications((prev) =>
        prev.map((n) => ({ ...n, readAt: n.readAt ?? now })),
      );
      setUnreadCount(0);
    } catch {
      // silently ignore
    }
  };

  const hasMore = notifications.length < total;
  const unreadInList = notifications.filter((n) => !n.readAt).length;

  return (
    <div className="mx-auto px-6 pt-16 pb-20 max-w-2xl">
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-2xl font-bold">Notifications</h1>
        {unreadInList > 0 && (
          <button
            onClick={handleMarkAllRead}
            className="flex items-center gap-2 text-sm text-accent hover:text-white transition-colors"
          >
            <CheckCheck className="w-4 h-4" />
            <span className="hidden md:inline">Mark all as read</span>
          </button>
        )}
      </div>

      {loading ? (
        <div className="flex justify-center py-16">
          <Loader2 className="w-8 h-8 animate-spin text-brand" />
        </div>
      ) : (
        <>
          <NotificationList
            notifications={notifications}
            onNotificationRead={handleNotificationRead}
          />

          {hasMore && (
            <div className="mt-6 flex justify-center">
              <button
                onClick={handleLoadMore}
                disabled={loadingMore}
                className="flex items-center gap-2 px-6 py-2.5 rounded-xl border border-white/10 text-sm text-accent hover:text-white hover:border-white/20 transition-all disabled:opacity-50"
              >
                {loadingMore ? (
                  <Loader2 className="w-4 h-4 animate-spin" />
                ) : (
                  `Load more (${total - notifications.length} remaining)`
                )}
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
