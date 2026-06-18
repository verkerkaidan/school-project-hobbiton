"use client";

import { getPagedPosts } from "@/queries/getPosts";
import { batchDoesLikeExist } from "@/lib/interactionClient";
import { FeedCard } from "./FeedCard";
import { useEffect, useRef, useState } from "react";
import { Post } from "@/types/posts";
import Masonry from "react-masonry-css";
import { breakpointColumns } from "@/constants/breakpointColumns";
import { useCurrentUser } from "@/hooks/useCurrentUser";
import { useInfiniteQuery } from "@tanstack/react-query";

export const FEED_QUERY_KEY = "feed";

const PAGE_SIZE = 16;

export const Feed = () => {
  const currentUser = useCurrentUser();
  const [likedIds, setLikedIds] = useState<Set<string>>(new Set());
  const sentinelRef = useRef<HTMLDivElement | null>(null);

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    isFetching,
  } = useInfiniteQuery({
    queryKey: [FEED_QUERY_KEY, currentUser?.id],
    queryFn: ({ pageParam }) =>
      getPagedPosts(pageParam as number, PAGE_SIZE, currentUser?.id),
    initialPageParam: 0,
    getNextPageParam: (lastPage, allPages) =>
      lastPage.length < PAGE_SIZE ? undefined : allPages.length,
    enabled: currentUser !== undefined,
  });

  const posts: Post[] = data?.pages.flat() ?? [];

  // Fetch liked state for any newly loaded posts.
  useEffect(() => {
    if (!currentUser || !data) return;
    const lastPage = data.pages.at(-1);
    if (!lastPage || lastPage.length === 0) return;
    const newIds = lastPage.map((p) => p.id);
    batchDoesLikeExist(currentUser.id, newIds)
      .then((liked) => setLikedIds((prev) => new Set([...prev, ...liked])))
      .catch(console.error);
  }, [data?.pages.length, currentUser]);

  // Infinite scroll sentinel.
  useEffect(() => {
    if (!hasNextPage) return;
    const sentinel = sentinelRef.current;
    if (!sentinel) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && !isFetchingNextPage) {
          fetchNextPage();
        }
      },
      { rootMargin: "200px" }
    );

    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [hasNextPage, isFetchingNextPage, fetchNextPage]);

  const loading = isFetching || isFetchingNextPage;

  return (
    <div>
      <Masonry
        breakpointCols={breakpointColumns}
        className="flex gap-4"
        columnClassName="flex flex-col"
      >
        {posts.map((post, index) => (
          <FeedCard
            key={post.id}
            item={post}
            index={index}
            liked={likedIds.has(post.id)}
          />
        ))}
      </Masonry>

      {/* Sentinel / status row */}
      <div ref={sentinelRef} className="flex justify-center py-8">
        {loading && (
          <span className="text-xs text-accent animate-pulse">Loading…</span>
        )}
        {!hasNextPage && posts.length > 0 && (
          <span className="text-xs text-accent opacity-40">You've seen it all.</span>
        )}
      </div>
    </div>
  );
};
