"use client";

import ChatCard from "@/components/chats/chatCard";
import NewChatModal from "@/components/chats/NewChatModal";
import { getChats } from "@/lib/chatClient";
import Cookies from "js-cookie";

import { useEffect, useState } from "react";

export default function ChatsPage() {
  const [chats, setChats] = useState<string[]>(
    []
  );

  const [modalOpen, setModalOpen] =
    useState(false);

  useEffect(() => {
    async function loadChats() {
      const userId = Cookies.get("userId");

      if (!userId) return;

      const data = await getChats(userId);

      setChats(data);
    }

    loadChats();
  }, []);

  return (
    <main className="min-h-screen bg-zinc-950 p-6">
      <div className="mx-auto max-w-7xl">
        <div className="mb-8 flex items-center justify-between">
          <div>
            <h1 className="text-4xl font-bold">
              Chats
            </h1>

            <p className="mt-2 text-zinc-400">
              Your conversations
            </p>
          </div>

          <button
            onClick={() =>
              setModalOpen(true)
            }
            className="rounded-2xl bg-blue-500 px-6 py-4 font-semibold text-white"
          >
            New Chat
          </button>
        </div>

        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {chats.map((chatId) => (
            <ChatCard
              key={chatId}
              chatId={chatId}
            />
          ))}
        </div>
      </div>

      <NewChatModal
        open={modalOpen}
        onClose={() =>
          setModalOpen(false)
        }
      />
    </main>
  );
}