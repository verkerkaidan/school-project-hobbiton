"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { createComment, getInteraction } from "@/lib/interactionClient";
import LikeButton from "@/components/interaction/LikeButton";
import { CompleteComment } from "@/types/interaction";
import Cookies from "js-cookie";
import { getProfile } from "@/queries/getProfile";

interface CompleteInteractionProps {
  postId: string;
}

const CompleteInteraction: React.FC<CompleteInteractionProps> = ({
  postId,
}) => {
  const [likes, setLikes] = useState<number>(0);
  const [comments, setComments] = useState<CompleteComment[]>([]);
  const [newComment, setNewComment] = useState<string>("");

  // async function syncInteraction() {
  //     const partialInteraction = await getInteraction(postId);
  //     const completeComments: CompleteComment[] = []; // Initialize a new array to store the complete comments
  //
  //     setLikes(partialInteraction.likes);
  //
  //     // Use Promise.all to wait for all profiles to be fetched before updating the state
  //     await Promise.all(partialInteraction.comments.map(async (comment) => {
  //         const profile = await getProfile(comment.userId);
  //         const completeComment: CompleteComment = {
  //             userData: profile,
  //             commentData: comment
  //         };
  //         completeComments.push(completeComment);
  //     }));
  //
  //     setComments(completeComments); // Now update the comments state after all profiles are fetched
  // }
  function syncInteraction() {
    getInteraction(postId).then(async (partialInteraction) => {
      const completeComments: CompleteComment[] = []; // Initialize a new array to store the complete comments

      setLikes(partialInteraction.likes);

      // Use Promise.all to wait for all profiles to be fetched before updating the state
      await Promise.all(
        partialInteraction.comments.map(async (comment) => {
          const profile = await getProfile(comment.userId);
          const completeComment: CompleteComment = {
            userData: profile,
            commentData: comment,
          };
          completeComments.push(completeComment);
        }),
      );

      setComments(completeComments); // Now update the comments state after all profiles are fetched
    });
  }

  // // Fetch likes and comments
  useEffect(() => {
    syncInteraction();
  }, [postId]);

  // Handle adding a new comment
  const handleCommentSubmit = async () => {
    if (!newComment.trim()) return;

    await createComment(Cookies.get("userId")!, postId, newComment);
    setNewComment("");
    syncInteraction();
    console.log("comments length:" + comments.length);
  };

  return (
    <div className="mt-8 space-y-6">
      {" "}
      {/* extra top margin and vertical spacing */}
      {/* Like Button */}
      <div className="mt-4">
        <LikeButton itemId={postId} initialLikes={likes} />
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
                  href={`/profile/${item.userData.id}`}
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
            className={`self-end px-4 py-2 rounded hover:bg-blue-600 transition ${
              !newComment.trim()
                ? "bg-gray-300 cursor-not-allowed"
                : "bg-blue-500 text-white"
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
