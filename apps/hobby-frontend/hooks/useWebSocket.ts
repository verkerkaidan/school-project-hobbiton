"use client";
const API_URL = process.env.NEXT_PUBLIC_WS_CHAT_SERVICE_URL;
const MIDDLEWARE_ENDPOINT_SERVERSIDE = process.env.NEXT_PUBLIC_MIDDLEWARE_URL_SERVERSIDE;
const MIDDLEWARE_ENDPOINT_CLIENTSIDE = process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE;

import { decodeMessage, encodeMessage } from "@/lib/chat";
import { useCallback, useEffect, useRef, useState } from "react";
import Cookies from "js-cookie";
import { useCurrentUser } from "./useCurrentUser";
import api from "@/lib/axios";

export type ChatMessage = { text: string; userId: string };

export function useWebSocket(chatId: string | null) {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const ws = useRef<WebSocket | null>(null);
  const reconnectTimer = useRef<NodeJS.Timeout | null>(null);
  const currentUser = useCurrentUser()!;
  const [userHasBeenConnected, setUserHasBeenConnected] = useState(false);

  useEffect(() => {
    if (!chatId) return;
    if (ws.current && ws.current.readyState !== WebSocket.CLOSED) return;

    const connect = () => {
      if (ws.current && ws.current.readyState !== WebSocket.CLOSED) return;
      if (!currentUser) return;

      ws.current = new WebSocket(`${API_URL}/ws/${chatId}?userId=${currentUser.id}&token=${currentUser.token}`);
      ws.current.binaryType = "arraybuffer";
      ws.current.onopen = () => {
        setIsConnected(true);
        setUserHasBeenConnected(true);
      };
      ws.current.onclose = (e: CloseEvent) => {
        setIsConnected(false);
        if (e.code === 1008) return;
        reconnectTimer.current = setTimeout(connect, 3000);
      };
      ws.current.onmessage = (e: MessageEvent<ArrayBuffer>) => {
        const msg = decodeMessage(e.data);
        setMessages((prev) => [...prev, msg]);
      };
    };

    connect();

    return () => {
      if (reconnectTimer.current) clearTimeout(reconnectTimer.current);
      ws.current?.close();
      ws.current = null;
    };
  }, [chatId, currentUser]);

  const send = useCallback((text: string) => {
    const userId: string = currentUser.id;
    if (!userId || ws.current?.readyState !== WebSocket.OPEN) return;

    const bytes = encodeMessage(text, userId);
    ws.current.send(bytes as unknown as Blob);
    setMessages((prev) => [...prev, { text, userId }]);
  }, []);

  const deleteUser = useCallback(async (userId: string) => {
    if (!chatId) return;
    await api.delete(`${MIDDLEWARE_ENDPOINT_CLIENTSIDE}/api/delete-user-from-chat`, {
      data: { userId, chatId },
    });
  }, [chatId]);

  const addUser = useCallback(async (userId: string) => {
    if (!chatId) return;

    await api.post(`${MIDDLEWARE_ENDPOINT_CLIENTSIDE}/api/add-user-to-chat`, {
      userId,
      chatId,
    });
  }, [chatId]);

  return { messages, isConnected, send, deleteUser, addUser, userHasBeenConnected };
}