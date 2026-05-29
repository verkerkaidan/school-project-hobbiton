"use client";

import MessageBubble from "@/components/chats/MessageBubble";
import { useCurrentUser } from "@/hooks/useCurrentUser";
import { useWebSocket } from "@/hooks/useWebSocket";
import { getChatTitle, updateChatTitle } from "@/lib/chatClient";
import Cookies from "js-cookie";
import Image from "next/image";
import { use, useEffect, useState } from "react";

export default function ChatPage({ params }: { params: Promise<{ chatId: string }> }) {
  const { chatId } = use(params);
  const currentUser = useCurrentUser()!;
  const { messages, send, isConnected } = useWebSocket(chatId);
  const [input, setInput] = useState("");
  const [title, setTitle] = useState("");
  const [isEditingTitle, setIsEditingTitle] = useState(false);
  const [titleInput, setTitleInput] = useState(title);
  function handleSend() {
    if (!input.trim()) return;
    send(input);
    setInput("");
  }

  useEffect(() => {
    getChatTitle(chatId).then(setTitle);
  }, [chatId]); // only re-fetches if chatId changes

  return (
    <div className="mx-auto px-4 pt-16 pb-20 max-w-3xl">
      <div className="border-b border-zinc-800 bg-zinc-900 px-6 py-5">
        {isEditingTitle ? (
          <input
            autoFocus
            value={titleInput}
            onChange={(e) => setTitleInput(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                updateChatTitle(chatId, titleInput) // your update function
                setTitle(titleInput); // update title state immediately
                setIsEditingTitle(false);
              }
              if (e.key === "Escape") setIsEditingTitle(false);
            }}
            onBlur={() => setIsEditingTitle(false)}
            className="text-xl font-bold text-white bg-transparent border-b border-zinc-500 outline-none"
          />
        ) : (
          <h1
            className="text-xl font-bold text-white cursor-pointer"
            onDoubleClick={() => {
              setTitleInput(title);
              setIsEditingTitle(true);
            }}
          >
            <div className="flex gap-[2vw]">
              {title.length > 2 ? (title) : "No title set"}
              <Image
                src="/svgs/pencil.svg"
                alt="Edit title"
                width={25}
                height={25}
                className=""
                onClick={() => setIsEditingTitle(true)}
              />
            </div>

          </h1>
        )}        <p className="mt-2 text-sm">
          {isConnected
            ? <span className="text-green-500">Connected</span>
            : <span className="text-red-500">Reconnecting...</span>}
        </p>
      </div>

      <div className="flex-1 space-y-4 overflow-y-auto py-6">
        {messages.map((msg, i) => (
          <MessageBubble key={i} message={msg} ownId={currentUser.id} />
        ))}
      </div>

      <div className="border-t border-zinc-800 bg-zinc-900 p-4">
        <div className="mx-auto flex max-w-5xl gap-3">
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleSend()}
            placeholder="Type a message..."
            className="flex-1 rounded-2xl border border-zinc-700 bg-zinc-800 px-5 py-4 text-white outline-none focus:border-blue-500"
          />
          <button
            onClick={handleSend}
            disabled={!isConnected}
            className="px-6 py-2.5 rounded-lg bg-brand text-white text-sm font-medium hover:opacity-90 transition-opacity duration-200"
          >
            Send
          </button>
        </div>
      </div>
    </div>
  );
}