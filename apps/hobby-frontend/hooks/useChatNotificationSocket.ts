"use client";

import { useCallback, useEffect, useRef } from "react";
import { buildChatNotificationWebSocketUrl } from "../lib/chatNotificationClient";
import { useCurrentUser } from "./useCurrentUser";

export type ChatUnreadCounts = Record<string, number>;

export interface UseChatNotificationSocketOptions {
    enabled?: boolean;
    onCounts?: (counts: ChatUnreadCounts) => void;
    onOpen?: () => void;
}

export function useChatNotificationSocket({
    enabled = true,
    onCounts,
    onOpen,
}: UseChatNotificationSocketOptions = {}) {
    const currentUser = useCurrentUser();
    const wsRef = useRef<WebSocket | null>(null);
    const reconnectDelay = useRef(1000);
    const isMounted = useRef(true);

    const onCountsRef = useRef(onCounts);
    const onOpenRef = useRef(onOpen);

    useEffect(() => {
        onCountsRef.current = onCounts;
    }, [onCounts]);

    useEffect(() => {
        onOpenRef.current = onOpen;
    }, [onOpen]);

    const connect = useCallback(() => {
        if (!enabled || !isMounted.current) return;
        if (!currentUser?.id) return;

        const url = buildChatNotificationWebSocketUrl(currentUser.id);
        if (!url) return;

        try {
            const ws = new WebSocket(url);
            wsRef.current = ws;

            ws.onopen = () => {
                reconnectDelay.current = 1000;
                // Flush any queued commands
                for (const cmd of pendingCommands.current) {
                    ws.send(JSON.stringify(cmd));
                }
                pendingCommands.current = [];
                onOpenRef.current?.();
            };

            ws.onmessage = (event) => {
                try {
                    const msg = JSON.parse(event.data as string);
                    if (msg.type === "unread_counts" && msg.data) {
                        onCountsRef.current?.(msg.data as ChatUnreadCounts);
                    }
                } catch {
                    // Ignore malformed messages
                }
            };

            ws.onclose = (event) => {
                wsRef.current = null;
                if (!isMounted.current) return;
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
    }, [currentUser?.id, enabled]);

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

    const pendingCommands = useRef<object[]>([]);

    const sendCommand = useCallback((payload: object) => {
        if (wsRef.current?.readyState === WebSocket.OPEN) {
            wsRef.current.send(JSON.stringify(payload));
        } else {
            pendingCommands.current.push(payload);
        }
    }, []);

    return {
        disconnect: () => {
            isMounted.current = false;
            wsRef.current?.close(1000, "manual disconnect");
        },
        markChatAsRead: (chatId: string) => {
            sendCommand({ type: "mark_chat_as_read", chatId });
        },
        markAllAsRead: () => {
            sendCommand({ type: "mark_all_as_read" });
        },
    };
}
