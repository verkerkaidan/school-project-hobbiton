"use client";

import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
} from "react";
import { fetchUnreadCount } from "@/lib/notificationClient";
import { useNotificationSocket } from "@/hooks/useNotificationSocket";
import { Notification } from "@/types/notifications";
import Cookies from "js-cookie";

type NotificationSubscriber = (notif: Notification) => void;

interface NotificationContextValue {
  unreadCount: number;
  setUnreadCount: React.Dispatch<React.SetStateAction<number>>;
  refresh: () => Promise<void>;
  subscribe: (cb: NotificationSubscriber) => () => void;
}

const NotificationContext = createContext<NotificationContextValue>({
  unreadCount: 0,
  setUnreadCount: () => {},
  refresh: async () => {},
  subscribe: () => () => {},
});

export function NotificationProvider({
  children,
}: {
  children: React.ReactNode;
}) {
  const [unreadCount, setUnreadCount] = useState(0);
  const subscribers = useRef<Set<NotificationSubscriber>>(new Set());
  const hasFetched = useRef(false);

  const subscribe = useCallback((cb: NotificationSubscriber) => {
    subscribers.current.add(cb);
    return () => subscribers.current.delete(cb);
  }, []);

  const refresh = useCallback(async () => {
    const token = Cookies.get("token");
    if (!token) return;
    try {
      const count = await fetchUnreadCount();
      setUnreadCount(count);
    } catch {
      // silently ignore
    }
  }, []);

  useEffect(() => {
    if (hasFetched.current) return;
    hasFetched.current = true;
    refresh();
  }, [refresh]);

  useNotificationSocket({
    onNotification: (notif) => {
      setUnreadCount((c) => c + 1);
      subscribers.current.forEach((cb) => cb(notif));
    },
    // Re-fetch the accurate count from the DB whenever the socket (re-)connects.
    // This closes the gap where a notification arrives during the ~500 ms WS
    // handshake window and would otherwise be missed until the next page load.
    onOpen: refresh,
  });

  return (
    <NotificationContext.Provider
      value={{ unreadCount, setUnreadCount, refresh, subscribe }}
    >
      {children}
    </NotificationContext.Provider>
  );
}

export function useNotifications() {
  return useContext(NotificationContext);
}
