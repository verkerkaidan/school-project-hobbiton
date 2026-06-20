import { authHeaders } from "@/lib/adminClient";

const GATEWAY_URL =
  process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE ?? "http://localhost:23738";

export async function updatePost(
  postId: string,
  payload: Record<string, unknown>,
  newFiles: File[] = []
): Promise<void> {
  const formData = new FormData();
  formData.append(
    "data",
    new Blob([JSON.stringify(payload)], { type: "application/json" })
  );
  newFiles.forEach((f) => formData.append("files[]", f));

  // Do not set Content-Type manually — the browser sets the multipart boundary.
  const res = await fetch(`${GATEWAY_URL}/api/posts/${postId}`, {
    method: "PUT",
    headers: authHeaders(),
    body: formData,
  });

  if (!res.ok) {
    const body = await res.text().catch(() => "");
    throw new Error(`Failed to update post (${res.status}): ${body}`);
  }
}

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
