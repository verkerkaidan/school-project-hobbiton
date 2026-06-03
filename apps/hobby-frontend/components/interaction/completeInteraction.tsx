"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { createComment } from "@/lib/interactionClient";
import LikeButton from "@/components/interaction/LikeButton";
import { CompleteComment, Comment, Interaction } from "@/types/interaction";
import Cookies from "js-cookie";
import { getProfile } from "@/queries/getProfile";
import {Post} from "@/types/posts";
import {getPostById} from "@/queries/getPostById";

interface CompleteInteractionProps {
  post: Post;
}

const CompleteInteraction: React.FC<CompleteInteractionProps> = ({
  post,
}) => {
  const [likes, setLikes] = useState<number>(0);
  const [comments, setComments] = useState<CompleteComment[]>([]);
  const [newComment, setNewComment] = useState<string>("");

    async function syncInteraction() {
        const freshPost = await getPostById(post.id);

        const completeComments: CompleteComment[] = await Promise.all(
            freshPost.interaction.comments.map(async (comment: Comment) => {
                const profile = await getProfile(comment.userId);
                return { userData: profile, commentData: comment } as CompleteComment;
            })
        );

        setComments(completeComments);
        setLikes(freshPost.interaction.likes);
  }

  // // Fetch likes and comments
  useEffect(() => {
    syncInteraction();
  }, [post]);

  // Handle adding a new comment
  const handleCommentSubmit = async () => {
    if (!newComment.trim()) return;

    await createComment(Cookies.get("userId")!, post.id, newComment);
    setNewComment("");
    syncInteraction();
  };

  return (
    <div className="mt-8 space-y-6">
      {" "}
      {/* extra top margin and vertical spacing */}
      {/* Like Button */}
      <div className="mt-4">
        <LikeButton itemId={post.id} initialLikes={likes} />
      </div>
      {/* Comments Section */}
      <div className="space-y-4">
        <h3 className="text-lg font-semibold">Comments</h3>

        <div className="space-y-4">
          {comments.length === 0 ? (
            <p className="text-sm text-accent">
              No comments yet. Be the first to comment!
            </p>
          ) : (
            comments.map((item, i) => (
              <div key={i} className="border border-gray rounded p-4 space-y-2">
                <p className="text-sm">{item.commentData.comment}</p>
                <Link
                  href={`/profile/${item.userData.username}`}
                  className="text-xs text-accent hover:underline"
                >
                  {item.userData.username}
                </Link>
              </div>
            ))
          )}
        </div>

        {/* Comment Input */}
        <div className="mt-4 flex flex-col gap-2">
          <textarea
            placeholder="Add a comment..."
            rows={3}
            className="w-full p-3 border rounded-md text-sm"
            value={newComment}
            onChange={(e) => setNewComment(e.target.value)}
          />
          <button
            onClick={handleCommentSubmit}
            disabled={!newComment.trim()} // Disable button if comment is empty
            className={`self-end px-4 py-2 rounded-xl transition ${
              !newComment.trim()
                ? "bg-gray-300 cursor-not-allowed"
                : "bg-brand text-white hover:bg-brand-light cursor-pointer"
            }`}
          >
            Submit
          </button>
        </div>
      </div>
    </div>
  );
};

export default CompleteInteraction;
