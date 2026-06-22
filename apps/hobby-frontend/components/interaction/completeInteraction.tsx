"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { createComment } from "@/lib/interactionClient";
import LikeButton from "@/components/interaction/LikeButton";
import { CompleteComment, Comment } from "@/types/interaction";
import Cookies from "js-cookie";
import { getProfile } from "@/queries/getProfile";
import { Post } from "@/types/posts";
import { getPostById } from "@/queries/getPostById";
import { motion, AnimatePresence } from "framer-motion";

interface CompleteInteractionProps {
    post: Post;
}

export default function CompleteInteraction({ post }: CompleteInteractionProps) {
    const [likes, setLikes] = useState<number>(0);
    const [comments, setComments] = useState<CompleteComment[]>([]);
    const [newComment, setNewComment] = useState<string>("");
    const [isSubmitting, setIsSubmitting] = useState<boolean>(false);

    async function syncInteraction() {
        try {
            const freshPost = await getPostById(post.id);
            const completeComments: CompleteComment[] = await Promise.all(
                freshPost.interaction.comments.map(async (comment: Comment) => {
                    const profile = await getProfile(comment.userId).catch(() => ({ username: "unknown" }));
                    return { userData: profile, commentData: comment } as CompleteComment;
                })
            );
            setComments(completeComments);
            setLikes(freshPost.interaction.likes);
        } catch (err) {}
    }

    useEffect(() => {
        syncInteraction();
    }, [post]);

    const handleCommentSubmit = async () => {
        if (!newComment.trim() || isSubmitting) return;
        setIsSubmitting(true);
        try {
            await createComment(Cookies.get("userId")!, post.id, newComment);
            setNewComment("");
            await syncInteraction();
        } catch (e) {

        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="max-w-4xl space-y-6">

            {/* Interaction Bar */}
            <div className="flex items-center gap-4 bg-white/[0.02] border border-white/5 p-4 rounded-2xl w-fit">
                <LikeButton itemId={post.id} initialLikes={likes} />
            </div>

            {/* Comments Container */}
            <div className="p-6 glass-card rounded-2xl space-y-6">
                <div className="flex items-center gap-2 border-b border-white/5 pb-4">
                    <h3 className="text-lg font-bold tracking-tight text-white">Discussion</h3>
                    <span className="text-xs bg-white/10 px-2 py-0.5 rounded-md opacity-60 font-mono">{comments.length}</span>
                </div>

                {/* Dynamic Comments List */}
                <div className="space-y-3">
                    <AnimatePresence mode="popLayout">
                        {comments.length === 0 ? (
                            <motion.p
                                initial={{ opacity: 0 }}
                                animate={{ opacity: 0.4 }}
                                className="text-sm italic text-center py-4"
                            >
                                No comments yet. Be the first to start the conversation!
                            </motion.p>
                        ) : (
                            comments.map((item, i) => (
                                <motion.div
                                    key={item.commentData.id || i}
                                    initial={{ opacity: 0, x: -10 }}
                                    animate={{ opacity: 1, x: 0 }}
                                    exit={{ opacity: 0, x: 10 }}
                                    transition={{ duration: 0.2 }}
                                    className="p-4 rounded-xl bg-white/[0.02] border border-white/5 flex flex-col gap-1.5"
                                >
                                    <p className="text-sm text-white/90 leading-relaxed">{item.commentData.comment}</p>
                                    <div className="flex items-center gap-1.5 mt-1">
                                        <span className="text-[10px] opacity-40">by</span>
                                        <Link
                                            href={`/profile/${item.userData?.username}`}
                                            className="text-xs text-[var(--color-brand-light)] font-medium hover:underline tracking-wide"
                                        >
                                            @{item.userData?.username || "user"}
                                        </Link>
                                    </div>
                                </motion.div>
                            ))
                        )}
                    </AnimatePresence>
                </div>

                {/* Premium Comment Field */}
                <div className="flex flex-col gap-3 pt-2">
          <textarea
              placeholder="Write a supportive comment..."
              rows={3}
              className="w-full bg-white/5 border border-white/10 focus:border-[var(--color-brand)] rounded-xl p-4 text-sm text-white placeholder-white/30 outline-none transition-colors resize-none"
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
          />
                    <button
                        onClick={handleCommentSubmit}
                        disabled={!newComment.trim() || isSubmitting}
                        className={`self-end px-5 py-2.5 rounded-xl font-semibold text-sm transition-all shadow-md ${
                            !newComment.trim() || isSubmitting
                                ? "bg-white/5 text-white/20 cursor-not-allowed border border-white/5"
                                : "bg-[var(--color-brand)] text-white hover:opacity-95 active:scale-[0.98] cursor-pointer"
                        }`}
                    >
                        {isSubmitting ? "Sending..." : "Post Comment"}
                    </button>
                </div>

            </div>
        </div>
    );
}