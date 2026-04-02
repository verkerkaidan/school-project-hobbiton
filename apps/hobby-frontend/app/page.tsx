"use client";
import { Feed } from '@/components/feed/Feed';
import { CreatePostModal } from '@/components/modals/newPost';
import { useCurrentUser } from '@/hooks/useCurrentUser';
import { useState } from 'react';

export default function HomePage() {
  const currentUser = useCurrentUser();
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div className="mx-auto px-6 pt-16 pb-20">
      <header className="mb-20 space-y-4">
        <h1 className="text-5xl md:text-7xl font-serif italic text-white">
          Welcome{" "}
          <span className="text-brand">{currentUser?.username ?? "..."}</span>
        </h1>
        <p className="text-accent text-lg max-w-sm">
          Your personal dose of reading, cinema en exercise.
        </p>
      </header>
        <button
            onClick={() => setIsOpen(true)}
            className="px-6 py-2.5 rounded-lg bg-brand text-white text-sm font-medium hover:opacity-90 transition-opacity duration-200"
        >
            Create new post!
        </button>

        <CreatePostModal isOpen={isOpen} onClose={() => setIsOpen(false)} />
        <Feed />
    </div>
  );
}
