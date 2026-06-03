"use client";
const API_URL = process.env.NEXT_PUBLIC_WS_CHAT_SERVICE_URL;
const MIDDLEWARE_ENDPOINT_SERVERSIDE = process.env.NEXT_PUBLIC_MIDDLEWARE_URL_SERVERSIDE;

import { decodeMessage, encodeMessage } from "@/lib/chat";
import { useCallback, useEffect, useRef, useState } from "react";
import Cookies from "js-cookie";
import { getProfile } from "@/queries/getProfile";
import { useCurrentUser } from "./useCurrentUser";

export type ChatMessage = { text: string; userId: string };

export function useWebSocket(chatId: string | null) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
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

      ws.current.onclose = (e: CloseEvent) => {
        setIsConnected(false);
        if (e.code === 1008) return;
        reconnectTimer.current = setTimeout(connect, 3000);
      };


      ws.current.onmessage = (e: MessageEvent<ArrayBuffer>) => {
        const msg = decodeMessage(e.data);
        setMessages((prev) => [...prev, msg]);
        // if user id = not in

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

  const deleteUser = useCallback(async (userId: string) => {
    if (!chatId) return;
    await fetch(`${MIDDLEWARE_ENDPOINT_SERVERSIDE}/api/delete-user-from-chat`, {
      method: "DELETE",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId, chatId }),
    });
  }, [chatId]);

  const addUser = useCallback(async (userId: string) => {
    if (!chatId) return;
    await fetch(`${MIDDLEWARE_ENDPOINT_SERVERSIDE}/api/add-user-to-chat`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId, chatId }),
    });
  }, [chatId]);


  return { messages, isConnected, send, deleteUser, addUser };
}