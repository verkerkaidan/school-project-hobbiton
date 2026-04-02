import ImageSlider from "@/components/ui/ImageSlider";
import { gqlRequest } from "@/lib/graphQlClient";
import {PostResponse, Gym, Reading} from "@/types/posts";
import CompleteInteraction from "@/components/interaction/completeInteraction";

interface PostPageProps {
    params: Promise<{ id: string }>; // ✅ should be a Promise
}

const GET_POST_BY_ID_QUERY = `
    query GetPostById($id: ID!) {
        getPostById(id: $id) {
            id
            title
            description
            userId
            createdAt
            updatedAt
            hobbyType
            ... on Gym {
                totalDuration
                exercises {
                    title
                    reps
                    weight
                    rpe
                }
            }
            ... on Reading {
                bookTitle
                pagesRead
                totalPagesRead
                bookFinished
            }
            mediaSet {
              relevantId
              fileData
              fileThumb
              contentType
              contentFlag
            }
        }
    }
`;

function isGym(post: PostResponse): post is Gym {
    return post.hobbyType === "GYM";
}

function isReading(post: PostResponse): post is Reading {
    return post.hobbyType === "READING";
}


export default async function PostPage({ params }: PostPageProps) {
    const { id } = await params;
    const { getPostById: post } = await gqlRequest<{ getPostById: PostResponse }>(
        GET_POST_BY_ID_QUERY,
        { id }
    );

    if (!post) {
        return <div>Post not found.</div>;
    }

    return (
        <main className="max-w-2xl mx-auto px-4 py-10">
              <ImageSlider />
            <span className="text-xs font-mono uppercase tracking-tighter text-accent">
                {post.hobbyType}
            </span>
            <h1 className="text-4xl font-semibold mt-2 mb-4">{post.title}</h1>
            <p className="text-accent text-sm leading-relaxed">{post.description}</p>

            {isGym(post) && (
                <div className="mt-6 space-y-3">
                    <h2 className="text-lg font-semibold">Exercises</h2>
                    {post.exercises?.map((ex, i) => (
                        <div key={i} className="border border-gray rounded p-4 text-sm space-y-1">
                            <p className="font-medium">{ex.title}</p>
                            <p className="text-accent">Reps: {ex.reps} · Weight: {ex.weight}kg · RPE: {ex.rpe}</p>
                        </div>
                    ))}
                </div>
            )}

            {isReading(post) && (
                <div className="mt-6 space-y-2 text-sm text-accent">
                    <p>📖 {post.bookTitle}</p>
                    <p>Pages read: {post.pagesRead} / {post.totalPagesRead}</p>
                    <p>Finished: {post.bookFinished ? "Yes" : "No"}</p>
                </div>
            )}

            <CompleteInteraction postId={post.id}/>
        </main>
    );
}