const GATEWAY_URL =
    process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE ?? "http://localhost:23738";

const API_BASE = `${GATEWAY_URL}/api`;

export async function toggleLike(userId: string, referenceId: string): Promise<boolean> {
    const res = await fetch(`${API_BASE}/interaction/${userId}/${referenceId}`, {
        method: "PUT",
    });

    if (!res.ok) throw new Error("Failed to report");

    return await res.json();
}

export async function doesLikeExist(userId:string, referenceId: string): Promise<boolean> {
    const res = await fetch(`${API_BASE}/interaction/${userId}/${referenceId}`, {
        method: "GET",
    });

    if (!res.ok) throw new Error("Failed to report");

    return await res.json();
}

export async function createComment(userId: string, referenceId: string, comment: string): Promise<string> {
    const res = await fetch(`${API_BASE}/interaction`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ userId, referenceId, comment }),
    });

    if (!res.ok) throw new Error("Failed to report");

    return await res.json();
}
