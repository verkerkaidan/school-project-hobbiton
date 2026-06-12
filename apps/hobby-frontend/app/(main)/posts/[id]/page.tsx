import ImageSlider from "@/components/ui/ImageSlider";
import CompleteInteraction from "@/components/interaction/completeInteraction";
import { PostAuthorCard } from "@/components/post/PostAuthorCard";
import { getProfile } from "@/queries/getProfile";
import { getPostById } from "@/queries/getPostById";
import { PostResponse, Gym, Reading } from "@/types/posts";
import PostPageClient from "@/components/post/PostPageClient";

interface PostPageProps {
    params: Promise<{ id: string }>;
}

function isGym(post: PostResponse): post is Gym {
    return post.hobbyType === "GYM";
}
function isReading(post: PostResponse): post is Reading {
    return post.hobbyType === "READING";
}

export default async function PostPage({ params }: PostPageProps) {
    const { id } = await params;
    const post = await getPostById(id);

    if (!post) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <p className="text-sm opacity-40" style={{ color: "var(--color-accent)" }}>Post not found.</p>
            </div>
        );
    }

    const author = await getProfile(post.userId).catch(() => null);

    return (
        <PostPageClient
            post={post}
            author={author}
            isGym={isGym(post)}
            isReading={isReading(post)}
            gymPost={isGym(post) ? (post as Gym) : undefined}
            readingPost={isReading(post) ? (post as Reading) : undefined}
        >
            <ImageSlider media={post.mediaSet} />
            {author && (
                <PostAuthorCard
                    authorId={author.id}
                    username={author.username}
                    profilePicture={author.profilePicture}
                />
            )}
            <CompleteInteraction post={post} />
        </PostPageClient>
    );
}