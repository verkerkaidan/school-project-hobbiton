import { authHeaders } from "@/lib/adminClient";

const GATEWAY_URL =
  process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE ?? "http://localhost:23738";

export async function deleteOwnPost(postId: string): Promise<void> {
  const res = await fetch(`${GATEWAY_URL}/api/posts/${postId}`, {
    method: "DELETE",
    headers: authHeaders(),
  });

  if (!res.ok) {
    const body = await res.text().catch(() => "");
    throw new Error(`Failed to delete post (${res.status}): ${body}`);
  }
}
