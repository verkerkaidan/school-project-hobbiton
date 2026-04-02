const BASE_URL = process.env.NEXT_PUBLIC_MEDIA_URL;

export async function reportFile(userId: string, instanceId: string): Promise<string> {
    const res = await fetch(`${BASE_URL}/api/media/report/${userId}/${instanceId}`, {
        method: "POST",
    });

    if (!res.ok) throw new Error("Failed to report");

    return await res.json();
}
