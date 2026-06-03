"use client";

import MessageBubble from "@/components/chats/MessageBubble";
import { ChatManageUsersModal } from "@/components/modals/ChatManageUsersModel";
import { useCurrentUser } from "@/hooks/useCurrentUser";
import { useWebSocket } from "@/hooks/useWebSocket";
import { fetchUsersFromChat, getChatTitle, updateChatTitle } from "@/lib/chatClient";
import { getFollowers } from "@/queries/getProfile";
import { User } from "@/types/profile";
import Cookies from "js-cookie";
import Image from "next/image";
import { use, useEffect, useMemo, useRef, useState } from "react";

export default function ChatPage({ params }: { params: Promise<{ chatId: string }> }) {
  const { chatId } = use(params);
  const currentUser = useCurrentUser()!;
  const { messages, send, isConnected, deleteUser, addUser } = useWebSocket(chatId);
  const [input, setInput] = useState("");
  const [title, setTitle] = useState("");
  const [isEditingTitle, setIsEditingTitle] = useState(false);
  const [titleInput, setTitleInput] = useState(title);
  const [chatManageUsersModelOpen, setChatManageUsersModelOpen] = useState(false);
  const [chatUsersList, setChatUsersList] = useState<User[]>([]);
  const [followers, setFollowers] = useState<User[]>([]);

  const chatUsersById = useMemo(
    () => Object.fromEntries(chatUsersList.map((user) => [user.id, user])),
    [chatUsersList],
  );

  const containerRef = useRef<HTMLDivElement | null>(null);
  const firstLoadRef = useRef(true);
  const [isAtBottom, setIsAtBottom] = useState(true);
  const [showScrollToBottom, setShowScrollToBottom] = useState(false);

  function handleSend() {
    if (!input.trim()) return;
    send(input);
    setInput("");
  }

  useEffect(() => {
    if (!currentUser) return;
    fetchUsersAndFollowers();
  }, [chatId, currentUser?.id]); // re-fetch when chatId or currentUser changes

  const fetchUsersAndFollowers = () => {
    if (!currentUser) return;
    getChatTitle(chatId).then(setTitle);
    fetchUsersFromChat(chatId).then(setChatUsersList);
    getFollowers(currentUser.id).then(setFollowers);
  };

  useEffect(() => {
    const el = containerRef.current;
    const scrollToBottom = () => {
      if (!el) return;
      el.scrollTo({ top: el.scrollHeight, behavior: "smooth" });
    };

    if (firstLoadRef.current) {
      // initial load: always scroll to bottom
      scrollToBottom();
      firstLoadRef.current = false;
      return;
    }

    if (isAtBottom) {
      scrollToBottom();
    } else {
      setShowScrollToBottom(true);
    }
  }, [messages, isAtBottom]);
  return (
    <div className="mx-auto px-4 pt-16 pb-20 max-w-3xl">
      <div className="rounded-t-xl border bg-background-secondary px-6 py-5">
        <div className="flex justify-between">
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
              className="text-xl font-bold text-white bg-transparent border-b border-zinc-500 outline-none w-fit"
            />
          ) : (
            <h1
              className="text-xl font-bold text-white cursor-pointer size-fit"
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
          )}

          <div className="flex gap-[1vw] cursor-pointer" onClick={() => { setChatManageUsersModelOpen(true) }}>
            <p>Manage users</p>
            <Image
              src="/svgs/bars.svg"
              alt="Edit title"
              width={25}
              height={25}
              className=""
            />
          </div>

        </div>
        <p className="mt-2 text-sm">
          {isConnected
            ? <span className="text-green-500">Connected</span>
            : <span className="text-red-500">Reconnecting...</span>}
        </p>
      </div>

      <div className="flex-1 relative">
        <div
          ref={containerRef}
          onScroll={() => {
            const el = containerRef.current;
            if (!el) return;
            const atBottom = el.scrollHeight - el.scrollTop - el.clientHeight < 60;
            setIsAtBottom(atBottom);
            if (atBottom) setShowScrollToBottom(false);
          }}
          className="space-y-4 overflow-y-auto py-6 px-5 h-[55vh]"
        >
          {messages.map((msg, i) => (
            <MessageBubble
              key={i}
              message={msg}
              own={msg.userId === currentUser.id}
              username={chatUsersById[msg.userId]?.username ?? "User no longer in chat"}
            />
          ))}
        </div>

        {showScrollToBottom && (
          <button
            onClick={() => {
              const el = containerRef.current;
              if (!el) return;
              el.scrollTo({ top: el.scrollHeight, behavior: "smooth" });
              setShowScrollToBottom(false);
            }}
            className="flex gap-2 absolute left-1/2 bottom-4 z-10 rounded-full bg-brand px-3 py-2 text-sm text-white shadow-lg transform -translate-x-1/2"          >
            <p>
              Scroll to bottom
            </p>
            <Image
              src="/svgs/down-arrow.svg"
              alt="Scroll to bottom"
              width={20}
              height={20}
              className=""
            />
          </button>
        )}
      </div>

      <div className="rounded-b-xl bg-background-secondary p-4">
        <div className="mx-auto flex max-w-5xl gap-3">
          <input
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleSend()}
            placeholder="Type a message..."
            className="flex-1 rounded-2xl bg-background px-5 py-4 text-white outline-none focus:border-blue-500"
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


      <ChatManageUsersModal
        isOpen={chatManageUsersModelOpen}
        onClose={() => setChatManageUsersModelOpen(false)}
        chatUsersList={chatUsersList}
        followers={followers}
        chatId={chatId}
        deleteUser={deleteUser}
        addUser={addUser}
        fetchUsersAndFollowers={fetchUsersAndFollowers}
        selfUser={currentUser}
      />
    </div>
  );
}