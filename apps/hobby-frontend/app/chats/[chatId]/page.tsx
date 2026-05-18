"use client";

import MessageBubble from "@/components/chats/MessageBubble";
import { useWebSocket } from "@/hooks/useWebSocket";
import Cookies from "js-cookie";
import { use, useState } from "react";

export default function ChatPage({ params }: { params: Promise<{ chatId: string }> }) {
  const { chatId } = use(params);
  const ownId = Cookies.get("uuid") ?? "";
  const { messages, send, isConnected } = useWebSocket(chatId);
  const [input, setInput] = useState("");

  function handleSend() {
    if (!input.trim()) return;
    send(input);
    setInput("");
  }

  return (
    <main className="flex h-screen flex-col bg-zinc-950">
      <div className="border-b border-zinc-800 bg-zinc-900 px-6 py-5">
        <h1 className="text-xl font-bold text-white">Chat</h1>
        <p className="mt-1 text-sm text-zinc-400">{chatId}</p>
        <p className="mt-2 text-sm">
          {isConnected
            ? <span className="text-green-500">Connected</span>
            : <span className="text-red-500">Reconnecting...</span>}
        </p>
      </div>

      <div className="flex-1 space-y-4 overflow-y-auto p-6">
        {messages.map((msg, i) => (
          <MessageBubble key={i} message={msg} ownId={ownId} />
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
            className="rounded-2xl bg-blue-500 px-6 font-semibold text-white disabled:opacity-50"
          >
            Send
          </button>
        </div>
      </div>
    </main>
  );
}