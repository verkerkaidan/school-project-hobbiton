'use client';
import { getAllPosts } from '@/queries/getPosts';
import {FeedCard} from './FeedCard';
import { useEffect, useState } from "react";

export const Feed = () => {

    const [posts, setPosts] = useState<Post[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        getAllPosts()
        .then(data => setPosts(data))
        .finally(() => setLoading(false));
    }, []); // ✅ empty array ensures this runs only once
    return (
        <div className="columns-1 md:columns-2 lg:columns-3 xl:columns-4 gap-6 space-y-6">
            {posts.map((post, index) => (
                <FeedCard key={index} item={post} index={index}/>
            ))}
        </div>
    );
};