import {Interaction} from "@/types/interaction";

const BASE_URL = process.env.NEXT_PUBLIC_INTERACTION_URL;

export async function toggleLike(userId: string, referenceId: string): Promise<boolean> {
    const res = await fetch(`${BASE_URL}/api/interaction/${userId}/${referenceId}`, {
        method: "PUT",
    });

    if (!res.ok) throw new Error("Failed to report");

    return await res.json();
}

export async function getInteraction(referenceId: string): Promise<Interaction> {
    const res = await fetch(`${BASE_URL}/api/interaction/${referenceId}`, {
        method: "GET",
    });

    if (!res.ok) throw new Error("Failed to report");

    return await res.json();
}

export async function doesLikeExist(userId:string, referenceId: string): Promise<boolean> {
    const res = await fetch(`${BASE_URL}/api/interaction/${userId}/${referenceId}`, {
        method: "GET",
    });

    if (!res.ok) throw new Error("Failed to report");

    return await res.json();
}

export async function createComment(userId: string, referenceId: string, comment: string): Promise<string> {
    const res = await fetch(`${BASE_URL}/api/interaction`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ userId, referenceId, comment }),
    });

    if (!res.ok) throw new Error("Failed to report");

    return await res.json();
}
