"use client";
import { getAllPosts } from "@/queries/getPosts";
import { FeedCard } from "./FeedCard";
import { useEffect, useState } from "react";
import {CompletePost} from "@/types/posts";
import {Interaction} from "@/types/interaction";
import {getInteraction} from "@/lib/interactionClient";

export const Feed = () => {
  const [posts, setPosts] = useState<CompletePost[]>([]);
  const [loading, setLoading] = useState(true);

  async function getPosts(): Promise<CompletePost[]> {
    const partialPosts = await getAllPosts();

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
    getPosts()
      .then((data) => setPosts(data))
      .finally(() => setLoading(false));
  }, []); // ✅ empty array ensures this runs only once
  return (
    <div className="columns-1 md:columns-2 lg:columns-3 xl:columns-4 gap-6 space-y-6">
      {posts.map((post, index) => (
        <FeedCard key={index} item={post.post} initialLikes={post.likes} index={index} />
      ))}
    </div>
  );
};
