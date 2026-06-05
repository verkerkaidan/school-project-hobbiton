"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { getProfile } from "@/queries/getProfile";
import { User } from "@/types/profile";

interface FeedCardAuthorProps {
  userId: string;
}

export function FeedCardAuthor({ userId }: FeedCardAuthorProps) {
  const [author, setAuthor] = useState<User | null>(null);

  useEffect(() => {
    getProfile(userId)
      .then(setAuthor)
      .catch(() => {});
  }, [userId]);

  if (!author) return null;

  return (
    <div className="flex items-center gap-2 mb-3">
      <Link
        href={`/profile/${author.username}`}
        onClick={(e) => e.stopPropagation()}
        className="text-xs font-medium text-accent hover:text-brand-light transition-colors duration-200"
      >
        @{author.username}
      </Link>
    </div>
  );
}
