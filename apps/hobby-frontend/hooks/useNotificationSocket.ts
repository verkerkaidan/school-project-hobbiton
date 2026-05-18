"use client";

import { useEffect, useRef, useCallback } from "react";
import { buildWebSocketUrl } from "@/lib/notificationClient";
import { Notification } from "@/types/notifications";

export interface UseNotificationSocketOptions {
  onNotification?: (notification: Notification) => void;
  /** Called whenever a new notification arrives so the consumer can increment its count. */
  onUnreadCountChange?: () => void;
  enabled?: boolean;
}

/**
 * Establishes and maintains a WebSocket connection to the notification-service.
 * Automatically reconnects on unexpected disconnection with exponential backoff.
 */
export function useNotificationSocket({
  onNotification,
  onUnreadCountChange,
  enabled = true,
}: UseNotificationSocketOptions = {}) {
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectDelay = useRef(1000);
  const isMounted = useRef(true);

  // Keep callback refs up-to-date without destabilising the socket connection
  const onNotificationRef = useRef(onNotification);
  const onUnreadCountChangeRef = useRef(onUnreadCountChange);
  useEffect(() => {
    onNotificationRef.current = onNotification;
  }, [onNotification]);
  useEffect(() => {
    onUnreadCountChangeRef.current = onUnreadCountChange;
  }, [onUnreadCountChange]);

  const connect = useCallback(() => {
    if (!isMounted.current || !enabled) return;

    const url = buildWebSocketUrl();
    if (!url || url.startsWith("ws/ws")) return; // no token yet

    try {
      const ws = new WebSocket(url);
      wsRef.current = ws;

      ws.onopen = () => {
        reconnectDelay.current = 1000; // reset backoff on successful connect
      };

      ws.onmessage = (event) => {
        try {
          const msg = JSON.parse(event.data as string);
          if (msg.type === "NOTIFICATION" && msg.payload) {
            const notif = msg.payload as Notification;
            onNotificationRef.current?.(notif);
            onUnreadCountChangeRef.current?.();
          }
        } catch {
          // Ignore malformed messages
        }
      };

      ws.onclose = (event) => {
        wsRef.current = null;
        if (!isMounted.current) return;
        // Only reconnect on abnormal closure (not deliberate disconnect)
        if (event.code !== 1000 && event.code !== 1001) {
          const delay = Math.min(reconnectDelay.current, 30000);
          reconnectDelay.current = delay * 2;
          setTimeout(connect, delay);
        }
      };

      ws.onerror = () => {
        ws.close();
      };
    } catch {
      // WebSocket not available (SSR) — silently ignore
    }
  }, [enabled]); // callbacks accessed via refs — no reconnect on re-render

  useEffect(() => {
    isMounted.current = true;
    connect();

    return () => {
      isMounted.current = false;
      if (wsRef.current) {
        wsRef.current.close(1000, "component unmounted");
      }
    };
  }, [connect]);

  return {
    disconnect: () => {
      isMounted.current = false;
      wsRef.current?.close(1000, "manual disconnect");
    },
  };
}
