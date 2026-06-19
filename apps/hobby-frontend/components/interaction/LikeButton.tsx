// components/LikeButton.tsx
"use client"
import { Heart } from "lucide-react";
import { useState, useEffect } from "react";
import Cookies from "js-cookie";
import { doesLikeExist, toggleLike } from "@/lib/interactionClient";
import { useErrorToast } from "@/components/ui/ErrorToastProvider";

interface LikeButtonProps {
    itemId: string;
    initialLikes: number;
    /** Pre-fetched liked state from the feed batch call — skips the per-mount request. */
    initialLiked?: boolean;
}

const LikeButton: React.FC<LikeButtonProps> = ({ itemId, initialLikes, initialLiked }) => {
    const [liked, setLiked] = useState(initialLiked ?? false);
    const [likes, setLikes] = useState(initialLikes);
    const { showErrorToast } = useErrorToast();

    // Only fetch individually when initialLiked was NOT provided (e.g. single-post page).
    useEffect(() => {
        if (initialLiked !== undefined) return;
        async function fetchLike() {
            const userId = Cookies.get("userId");
            if (!userId) return;
            const exists = await doesLikeExist(userId, itemId);
            setLiked(exists);
        }
        fetchLike();
    }, [itemId, initialLiked]);

    useEffect(() => {
        setLikes(initialLikes);
    }, [initialLikes]);

    // Sync liked state when the pre-fetched value changes (new page loaded).
    useEffect(() => {
        if (initialLiked !== undefined) {
            setLiked(initialLiked);
        }
    }, [initialLiked]);

    const onToggleLike = async () => {
        setLiked((prev) => !prev);
        try {
            const likeNowExists = await toggleLike(Cookies.get("userId")!, itemId);
            setLikes((prev) => likeNowExists ? prev + 1 : prev - 1);
        } catch (e) {
            const error = e instanceof Error ? e.message : "Could not update";
            showErrorToast(error, "", "error");
            setLiked((prev) => !prev);
        }
    };

    return (
        <div className="flex gap-4 text-accent">
            <Heart
                onClick={(e) => {
                    e.stopPropagation();
                    onToggleLike();
                }}
                className={`w-5 h-5 cursor-pointer transition-all duration-200 hover:scale-110 ${
                    liked
                        ? "fill-rose-500 text-rose-500 stroke-rose-500"
                        : "text-accent"
                }`}
            />
            <span className="text-sm font-medium text-stone-700">{likes}</span>
        </div>
    );
};

export default LikeButton;
