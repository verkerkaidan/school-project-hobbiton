const BASE_URL = process.env.NEXT_PUBLIC_MEDIA_URL;

export async function reportFile(userId: string, instanceId: string): Promise<string> {
    console.log(BASE_URL);
    const res = await fetch(`${BASE_URL}/api/media/report/${userId}/${instanceId}`, {
        method: "POST",
    });

    if (!res.ok) throw new Error("Failed to report");

    return await res.json();
}
