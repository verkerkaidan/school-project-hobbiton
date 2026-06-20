import { getPostById } from "@/queries/getPostById";
import EditPostPageClient from "@/components/post/EditPostPageClient";

interface EditPostPageProps {
    params: Promise<{ id: string }>;
}

export default async function EditPostPage({ params }: EditPostPageProps) {
    const { id } = await params;
    const post = await getPostById(id);

    if (!post) {
        return (
            <div className="flex items-center justify-center min-h-screen">
                <p className="text-sm opacity-40" style={{ color: "var(--color-accent)" }}>Post not found.</p>
            </div>
        );
    }

    return <EditPostPageClient post={post} />;
}
