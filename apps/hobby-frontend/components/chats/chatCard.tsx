"use client";

import Link from "next/link";

type ChatCardProps = {
  chatId: string;
  title: string;
  unreadCount?: number;
  onOpenAction?: () => void;
};

export default function ChatCard({
  chatId,
  title,
  unreadCount = 0,
  onOpenAction,
}: ChatCardProps) {
  return (
    <Link
      href={`/chats/${chatId}`}
      className="rounded-3xl border bg-background p-5 transition hover:bg-brand relative"
      onClick={() => onOpenAction?.()}
    >
      <p className="text-sm text-zinc-500">
        Chat
      </p>
      <h2 className="mt-2 break-all text-lg font-semibold text-white">
        {title.length > 2 ? title : chatId}
      </h2>
      {unreadCount > 0 && (
        <span className="absolute top-4 right-4 bg-rose-500 text-white text-[11px] font-bold px-2 py-1 rounded-full">
          {unreadCount > 99 ? "99+" : unreadCount}
        </span>
      )}
    </Link>
  );
}