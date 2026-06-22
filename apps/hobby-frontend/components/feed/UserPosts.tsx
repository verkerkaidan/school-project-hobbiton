"use client";

import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { FeedCard } from "./FeedCard";
import { Post } from "@/types/posts";
import Masonry from "react-masonry-css";
import { breakpointColumns } from "@/constants/breakpointColumns";

interface UserPostsProps {
  userId: string;
  getPosts: (userId: string) => Promise<Post[]>;
  onPostsLoaded?: (count: number) => void;
}

export const UserPosts = ({
  userId,
  getPosts,
  onPostsLoaded,
}: UserPostsProps) => {
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;

    getPosts(userId)
      .then((data) => {
        if (cancelled) return;
        setPosts(data);
        onPostsLoaded?.(data.length);
      })
      .catch((error) => {
        if (cancelled) return;
        setPosts([]);
        onPostsLoaded?.(0);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [userId, getPosts, onPostsLoaded]);

  if (loading) {
    return (
      <div className="flex justify-center items-center py-20">
        <div className="text-accent">Loading posts...</div>
      </div>
    );
  }

  if (posts.length === 0) {
    return (
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        className="text-center py-20"
      >
        <p className="text-accent text-lg">No posts yet</p>
      </motion.div>
    );
  }

  return (
    <Masonry
      breakpointCols={breakpointColumns}
      className="flex gap-4"
      columnClassName="flex flex-col"
    >
      {posts.map((post, index) => (
        <FeedCard key={index} item={post} index={index} />
      ))}
    </Masonry>
  );
};
