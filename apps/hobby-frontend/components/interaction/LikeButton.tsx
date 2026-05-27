// components/LikeButton.tsx
"use client"
import { Heart } from "lucide-react";
import { useState, useEffect } from "react";
import Cookies from "js-cookie";
import {doesLikeExist, toggleLike} from "@/lib/interactionClient";

interface LikeButtonProps {
    itemId: string; // The id of the item to like/unlike
    initialLikes: number; // Initial number of likes
}

const LikeButton: React.FC<LikeButtonProps> = ({ itemId, initialLikes }) => {
    const [liked, setLiked] = useState(false);
    const [likes, setLikes] = useState(initialLikes);

    // Check if the user has already liked the item on mount
    useEffect(() => {
        async function fetchLike() {
            const userId = Cookies.get("userId");
            if (!userId) return;
            const exists = await doesLikeExist(userId, itemId);
            setLiked(exists);
        }
        fetchLike();
    }, [itemId]);

    useEffect(() => {
        setLikes(initialLikes);
    }, [initialLikes]);

    // Toggle the like status and update likes count
    const onToggleLike = async () => {
        setLiked((prev) => !prev);
        const likeNowExists = await toggleLike(Cookies.get("userId")!, itemId);
        setLikes((prev) => likeNowExists ? prev + 1 : prev - 1);
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
            {/* Small textbox showing number of likes */}
            <span className="text-sm font-medium text-stone-700">{likes}</span>
        </div>
    );
};

export default LikeButton;