"use client";

import { Feed } from "@/components/feed/Feed";
import { useCurrentUser } from "@/hooks/useCurrentUser";

export default function HomePage() {
  const currentUser = useCurrentUser();

  return (
    <div className="mx-auto px-6 pt-16 pb-20">
      <header className="mb-20 space-y-4">
        <h1 className="text-5xl md:text-7xl font-serif italic text-white">
          Welcome{" "}
          <span className="text-brand">{currentUser?.username ?? "..."}</span>
        </h1>
        <p className="text-accent text-lg max-w-sm">
          Jouw persoonlijke stroom van literatuur, cinema en beweging.
        </p>
      </header>
      <Feed />
    </div>
  );
}
