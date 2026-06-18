"use client";

import { getPagedPosts } from "@/queries/getPosts";
import { batchDoesLikeExist } from "@/lib/interactionClient";
import { FeedCard } from "./FeedCard";
import { useEffect, useRef, useState, useCallback } from "react";
import { Post } from "@/types/posts";
import Masonry from "react-masonry-css";
import { breakpointColumns } from "@/constants/breakpointColumns";
import Cookies from "js-cookie";

const PAGE_SIZE = 16;

export const Feed = () => {
  const [posts, setPosts] = useState<Post[]>([]);
  const [likedIds, setLikedIds] = useState<Set<string>>(new Set());
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [hasMore, setHasMore] = useState(true);

  // Sentinel element at the bottom of the feed — when it enters the viewport,
  // we load the next page.
  const sentinelRef = useRef<HTMLDivElement | null>(null);

  const loadPage = useCallback(async (pageIndex: number) => {
    if (loading) return;
    setLoading(true);
    try {
      const newPosts = await getPagedPosts(pageIndex, PAGE_SIZE);
      if (newPosts.length < PAGE_SIZE) {
        setHasMore(false);
      }

      const userId = Cookies.get("userId");
      let newLiked = new Set<string>();
      if (userId && newPosts.length > 0) {
        newLiked = await batchDoesLikeExist(userId, newPosts.map((p) => p.id));
      }

      setPosts((prev) => {
        const existingIds = new Set(prev.map((p) => p.id));
        const deduped = newPosts.filter((p) => !existingIds.has(p.id));
        return [...prev, ...deduped];
      });
      setLikedIds((prev) => new Set([...prev, ...newLiked]));
    } catch (err) {
      console.error("[Feed] Failed to load posts:", err);
    } finally {
      setLoading(false);
    }
  }, [loading]);

  // Load the first page on mount.
  useEffect(() => {
    loadPage(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  // Observe the sentinel — when it appears, increment the page counter.
  useEffect(() => {
    if (!hasMore) return;
    const sentinel = sentinelRef.current;
    if (!sentinel) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && !loading && hasMore) {
          setPage((prev) => prev + 1);
        }
      },
      { rootMargin: "200px" }
    );

    observer.observe(sentinel);
    return () => observer.disconnect();
  }, [hasMore, loading]);

  // When the page counter increments, fetch the next page.
  useEffect(() => {
    if (page === 0) return;
    loadPage(page);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

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
        {!hasMore && posts.length > 0 && (
          <span className="text-xs text-accent opacity-40">You've seen it all.</span>
        )}
      </div>
    </div>
  );
};
