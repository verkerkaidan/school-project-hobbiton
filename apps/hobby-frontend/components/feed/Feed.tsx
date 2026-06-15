"use client";
import { getAllPosts } from "@/queries/getPosts";
import { FeedCard } from "./FeedCard";
import { useEffect, useState } from "react";
import { Post } from "@/types/posts";
import Masonry from "react-masonry-css";
import { breakpointColumns } from "@/constants/breakpointColumns";

export const Feed = () => {
  const [posts, setPosts] = useState<Post[]>([]);
  const [loading, setLoading] = useState(true);

  async function getPosts(): Promise<Post[]> {
    return await getAllPosts();
  }


  useEffect(() => {
    getPosts()
      .then((data) => setPosts(data))
      .finally(() => setLoading(false));
  }, []);
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
