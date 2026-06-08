"use client";

const API_URL = process.env.NEXT_PUBLIC_WS_NOTIFICATION_SERVICE_URL;

import { useEffect, useRef } from "react";
import { Notification } from "@/types/notifications";
import { useCurrentUser } from "./useCurrentUser";

export interface UseNotificationSocketOptions {
  onNotification?: (notification: Notification) => void;
  onUnreadCountChange?: () => void;
  onOpen?: () => void;
  enabled?: boolean;
}

export function useNotificationSocket({
  onNotification,
  onUnreadCountChange,
  onOpen,
  enabled = true,
}: UseNotificationSocketOptions = {}) {
  const ws = useRef<WebSocket | null>(null);
  const reconnectTimer = useRef<NodeJS.Timeout | null>(null);

  const onNotificationRef = useRef(onNotification);
  const onUnreadCountChangeRef = useRef(onUnreadCountChange);
  const onOpenRef = useRef(onOpen);
  const currentUser = useCurrentUser();
  useEffect(() => { onNotificationRef.current = onNotification; }, [onNotification]);
  useEffect(() => { onUnreadCountChangeRef.current = onUnreadCountChange; }, [onUnreadCountChange]);
  useEffect(() => { onOpenRef.current = onOpen; }, [onOpen]);

  useEffect(() => {
    if (!enabled) return;
    if (ws.current && ws.current.readyState !== WebSocket.CLOSED) return;

    const connect = () => {
      if (!currentUser) return;
      if (ws.current && ws.current.readyState !== WebSocket.CLOSED) return;
      if (!API_URL) return;

      ws.current = new WebSocket(`${API_URL}/ws/notifications/${currentUser.id}`);

      ws.current.onopen = () => {
        onOpenRef.current?.();
      };

      ws.current.onclose = (e: CloseEvent) => {
        ws.current = null;
        if (e.code === 1000 || e.code === 1001) return;
        reconnectTimer.current = setTimeout(connect, 3000);
      };

      ws.current.onmessage = (e: MessageEvent<string>) => {
        try {
          const msg = JSON.parse(e.data);
          if (msg.type === "NOTIFICATION" && msg.payload) {
            onNotificationRef.current?.(msg.payload as Notification);
            onUnreadCountChangeRef.current?.();
          }
        } catch {
          // Ignore malformed messages
        }
      };

      ws.current.onerror = () => {
        ws.current?.close();
      };
    };

    connect();

    return () => {
      if (reconnectTimer.current) clearTimeout(reconnectTimer.current);
      ws.current?.close(1000, "component unmounted");
      ws.current = null;
    };
  }, [enabled]);

  return {
    disconnect: () => {
      if (reconnectTimer.current) clearTimeout(reconnectTimer.current);
      ws.current?.close(1000, "manual disconnect");
      ws.current = null;
    },
  };
}