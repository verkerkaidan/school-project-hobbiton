"use client";

import Link from "next/link";

export default function ChatCard({
  chatId,
  title
}: {
  chatId: string;
  title: string;

}) {
  return (
    <Link
      href={`/chats/${chatId}`}
      className="rounded-3xl border bg-background p-5 transition hover:bg-brand"
    >
      <p className="text-sm text-zinc-500">
        Chat
      </p>
      <h2 className="mt-2 break-all text-lg font-semibold text-white">
        {title.length > 2 ? title : chatId}
      </h2>
    </Link>
  );
}