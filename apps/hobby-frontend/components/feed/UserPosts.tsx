"use client";

import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { FeedCard } from "./FeedCard";
import {Post} from "@/types/posts";

interface UserPostsProps {
  userId: string;
  getPosts: (userId: string) => Promise<Post[]>;
}

export const UserPosts = ({ userId, getPosts }: UserPostsProps) => {
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);

  async function getCompletePosts(userId: string): Promise<Post[]> {
    return await getPosts(userId);
  }

  useEffect(() => {
    getCompletePosts(userId)
      .then((data) => setPosts(data))
      .catch((error) => {
        console.error("Failed to load user posts:", error);
        setPosts([]);
      })
      .finally(() => setLoading(false));
  }, [userId, getPosts]);

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
    <div className="columns-1 md:columns-2 lg:columns-3 xl:columns-4 gap-6 space-y-6">
      {posts.map((post, index) => (
        <motion.div
          key={index}
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: index * 0.05 }}
        >
          <FeedCard item={post} initialLikes={post.interaction.likes} index={index} />
        </motion.div>
      ))}
    </div>
  );
};
