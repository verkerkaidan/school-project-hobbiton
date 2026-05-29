"use client";
const API_URL = process.env.NEXT_PUBLIC_WS_CHAT_SERVICE_URL;

import { decodeMessage, encodeMessage } from "@/lib/chat";
import { useCallback, useEffect, useRef, useState } from "react";
import Cookies from "js-cookie";
import { getProfile } from "@/queries/getProfile";
import { useCurrentUser } from "./useCurrentUser";

export type ChatMessage = { text: string; userId: string };
interface ChatUser {
  username: string;
}

export function useWebSocket(chatId: string | null) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const knownUserIds = useRef<Set<string>>(new Set());
  const [chatUsersList, setChatUsersList] = useState<Record<string, ChatUser>>({});
  const [isConnected, setIsConnected] = useState(false);
  const ws = useRef<WebSocket | null>(null);
  const reconnectTimer = useRef<NodeJS.Timeout | null>(null); // ← moved to ref
  const currentUser = useCurrentUser()!;

  useEffect(() => {
    if (!chatId) return;

    // ← Guard: don't open a second connection if one is already open/connecting
    if (ws.current && ws.current.readyState !== WebSocket.CLOSED) return;

    const connect = () => {
      // ← Also guard inside reconnect logic
      if (ws.current && ws.current.readyState !== WebSocket.CLOSED) return;
      if (!currentUser) return; // ← add this guard

      ws.current = new WebSocket(`${API_URL}/ws/${chatId}?userId=${currentUser.id}`);
      ws.current.binaryType = "arraybuffer";

      ws.current.onopen = () => setIsConnected(true);

      ws.current.onclose = () => {
        setIsConnected(false);
        reconnectTimer.current = setTimeout(connect, 3000);
      };

      ws.current.onmessage = (e: MessageEvent<ArrayBuffer>) => {
        const msg = decodeMessage(e.data);
        setMessages((prev) => [...prev, msg]);
        // if user id = not in
        if (!knownUserIds.current.has(msg.userId!) && msg.userId != currentUser.id) {
          knownUserIds.current.add(msg.userId!);
          getProfile(msg.userId).then((userProfile) =>
            setChatUsersList((prev) => ({
              ...prev,
              [msg.userId]: { username: userProfile.username },
            }))
          );
        }
      }
    };

    connect();

    return () => {
      if (reconnectTimer.current) clearTimeout(reconnectTimer.current);
      ws.current?.close();
      ws.current = null; // ← clear the ref so the guard resets cleanly
    };
  }, [chatId, currentUser]);

  const send = useCallback((text: string) => {
    const userId = Cookies.get("userId");
    if (!userId || ws.current?.readyState !== WebSocket.OPEN) return;

    const bytes = encodeMessage(text, userId);
    ws.current.send(bytes as unknown as Blob);
    setMessages((prev) => [...prev, { text, userId }]);
  }, []);

  return { messages, isConnected, send, chatUsersList };
}