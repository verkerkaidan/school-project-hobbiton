'use client';
import {FeedCard} from './FeedCard';
import {Post} from '@/types';

interface FeedProps {
    posts: Post[];
}

export const Feed = ({posts}: FeedProps) => {
    return (
        <div className="columns-1 lg:columns-2 gap-6 space-y-6">
            {posts.map((post, index) => (
                <FeedCard key={index} item={post} index={index}/>
            ))}
        </div>
    );
};