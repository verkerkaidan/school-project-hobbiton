"use client";

import React, { useEffect, useState } from "react";
import { motion } from "framer-motion";
import { FeedCard } from "./FeedCard";
import {CompletePost, Post} from "@/types/posts";
import {Interaction} from "@/types/interaction";
import {getInteraction} from "@/lib/interactionClient";

interface UserPostsProps {
  userId: string;
  getPosts: (userId: string) => Promise<Post[]>;
}

export const UserPosts = ({ userId, getPosts }: UserPostsProps) => {
  const [posts, setPosts] = useState<CompletePost[]>([]);
  const [loading, setLoading] = useState(true);

  async function getCompletePosts(userId: string): Promise<CompletePost[]> {
    const partialPosts = await getPosts(userId);

    return await Promise.all(
        partialPosts.map(async (post) => {
          const interaction: Interaction = await getInteraction(post.id);

          const completePost: CompletePost = {
            post,
            likes: interaction.likes,
            comments: interaction.comments
          };

          return completePost;
        })
    );
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
          <FeedCard item={post.post} initialLikes={post.likes} index={index} />
        </motion.div>
      ))}
    </div>
  );
};
