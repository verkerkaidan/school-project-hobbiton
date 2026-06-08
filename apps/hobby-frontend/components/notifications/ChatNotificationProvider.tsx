"use client";

import React, { createContext, useCallback, useContext, useMemo, useState } from "react";
import { useChatNotificationSocket, ChatUnreadCounts } from "@/hooks/useChatNotificationSocket";
import { useCurrentUser } from "@/hooks/useCurrentUser";

interface ChatNotificationContextValue {
  chatUnreadCounts: ChatUnreadCounts;
  unreadTotal: number;
  markChatAsRead: (chatId: string) => void;
  markAllChatsAsRead: () => void;
}

const ChatNotificationContext = createContext<ChatNotificationContextValue>({
  chatUnreadCounts: {},
  unreadTotal: 0,
  markChatAsRead: () => {},
  markAllChatsAsRead: () => {},
});

export function ChatNotificationProvider({ children }: { children: React.ReactNode }) {
  const [chatUnreadCounts, setChatUnreadCounts] = useState<ChatUnreadCounts>({});
  const currentUser = useCurrentUser();

  const { markChatAsRead: sendMarkChatAsRead, markAllAsRead } = useChatNotificationSocket({
    enabled: !!currentUser,
    onCounts: (counts) => setChatUnreadCounts(counts),
  });

  const markChatAsRead = useCallback(
    (chatId: string) => {
      setChatUnreadCounts((prev) => {
        if (!prev[chatId]) return prev;
        return { ...prev, [chatId]: 0 };
      });
      sendMarkChatAsRead(chatId);
    },
    [sendMarkChatAsRead],
  );

  const markAllChatsAsRead = useCallback(() => {
    setChatUnreadCounts((prev) =>
      Object.keys(prev).length === 0
        ? prev
        : Object.fromEntries(Object.keys(prev).map((id) => [id, 0])) as ChatUnreadCounts,
    );
    markAllAsRead();
  }, [markAllAsRead]);

  const unreadTotal = useMemo(
    () => Object.values(chatUnreadCounts).reduce((sum, count) => sum + (count || 0), 0),
    [chatUnreadCounts],
  );

  return (
    <ChatNotificationContext.Provider
      value={{ chatUnreadCounts, unreadTotal, markChatAsRead, markAllChatsAsRead }}
    >
      {children}
    </ChatNotificationContext.Provider>
  );
}

export function useChatNotifications() {
  return useContext(ChatNotificationContext);
}
