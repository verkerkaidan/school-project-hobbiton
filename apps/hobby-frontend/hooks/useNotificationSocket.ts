"use client";

import { useEffect, useRef, useCallback } from "react";
import { buildWebSocketUrl } from "@/lib/notificationClient";
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
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectDelay = useRef(1000);
  const isMounted = useRef(true);
  const reconnectTimer = useRef<NodeJS.Timeout | null>(null);

  const currentUser = useCurrentUser();

  const onNotificationRef = useRef(onNotification);
  const onUnreadCountChangeRef = useRef(onUnreadCountChange);
  const onOpenRef = useRef(onOpen);

  useEffect(() => {
    onNotificationRef.current = onNotification;
  }, [onNotification]);

  useEffect(() => {
    onUnreadCountChangeRef.current = onUnreadCountChange;
  }, [onUnreadCountChange]);

  useEffect(() => {
    onOpenRef.current = onOpen;
  }, [onOpen]);

  const connect = useCallback(() => {
    if (!isMounted.current || !enabled) return;

    const url = buildWebSocketUrl();

    // IMPORTANT: preserve old safety check
    if (!url || url.startsWith("ws/ws")) return;

    if (wsRef.current && wsRef.current.readyState !== WebSocket.CLOSED) {
      return;
    }

    try {
      const ws = new WebSocket(url);
      wsRef.current = ws;

      ws.onopen = () => {
        reconnectDelay.current = 1000;
        onOpenRef.current?.();
      };

      ws.onmessage = (event) => {
        try {
          const msg = JSON.parse(event.data);

          if (msg.type === "NOTIFICATION" && msg.payload) {
            onNotificationRef.current?.(msg.payload as Notification);
            onUnreadCountChangeRef.current?.();
          }
        } catch {
          // ignore malformed messages
        }
      };

      ws.onclose = (event) => {
        wsRef.current = null;

        if (!isMounted.current) return;

        if (event.code === 1000 || event.code === 1001) return;

        const delay = Math.min(reconnectDelay.current, 30000);
        reconnectDelay.current *= 2;

        reconnectTimer.current = setTimeout(connect, delay);
      };

      ws.onerror = () => {
        ws.close();
      };
    } catch {
      // SSR safety
    }
  }, [enabled, currentUser]);

  useEffect(() => {
    isMounted.current = true;

    connect();

    return () => {
      isMounted.current = false;

      if (reconnectTimer.current) {
        clearTimeout(reconnectTimer.current);
      }

      wsRef.current?.close(1000, "unmount");
      wsRef.current = null;
    };
  }, [connect]);

  return {
    disconnect: () => {
      isMounted.current = false;

      if (reconnectTimer.current) {
        clearTimeout(reconnectTimer.current);
      }

      wsRef.current?.close(1000, "manual");
      wsRef.current = null;
    },
  };
}
