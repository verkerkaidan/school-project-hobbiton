"use client";

import ChatCard from "@/components/chats/chatCard";
import NewChatModal from "@/components/chats/NewChatModal";
import { getChats } from "@/lib/chatClient";
import { useChatNotifications } from "@/components/notifications/ChatNotificationProvider";
import Cookies from "js-cookie";
import { useEffect, useState } from "react";
import Loading from "../loading";

export default function ChatsPage() {
  const [chats, setChats] = useState<{ chatId: string; title: string }[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const { chatUnreadCounts, unreadTotal, markChatAsRead, markAllChatsAsRead } = useChatNotifications();

  useEffect(() => {
    async function loadChats() {
      const userId = Cookies.get("userId");
      if (!userId) return;

      const data = await getChats(userId);
      setChats(data);
      setIsLoading(false);
    }

    loadChats();
  }, []);

  if (isLoading) {
    return <Loading />;
  }

  return (
    <div className="mx-auto px-4 pt-16 pb-20 max-w-3xl">
      <div className="mx-auto max-w-7xl">
        <div className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-bold">Chats</h1>
            <p className="mt-2 text-zinc-400">Your conversations</p>
          </div>

          <div className="flex gap-3">
            {unreadTotal > 0 && (
              <button
                onClick={() => markAllChatsAsRead()}
                className="px-4 py-2 rounded-lg border border-zinc-700 text-sm text-white hover:border-brand hover:text-brand transition-colors duration-200"
              >
                Mark all as read
              </button>
            )}
            <button
              onClick={() => setModalOpen(true)}
              className="px-6 py-2.5 rounded-lg bg-brand text-white text-sm font-medium hover:opacity-90 transition-opacity duration-200"
            >
              New Chat
            </button>
          </div>
        </div>

        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {chats.map((chat) => (
            <ChatCard
              key={chat.chatId}
              chatId={chat.chatId}
              title={chat.title}
              unreadCount={chatUnreadCounts[chat.chatId] ?? 0}
            />
          ))}
        </div>
      </div>

      <NewChatModal open={modalOpen} onClose={() => setModalOpen(false)} />
    </div>
  );
}