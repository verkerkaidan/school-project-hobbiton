import {FileReport} from "@/types/reports";
import {authHeaders} from "@/lib/adminClient";

const GATEWAY_URL =
    process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE ?? "http://localhost:23738";

const API_BASE = `${GATEWAY_URL}/api`;
export async function reportFile(userId: string, instanceId: string): Promise<string> {
    const res = await fetch(`${API_BASE}/media/report/${userId}/${instanceId}`, {
        method: "POST",
        headers: authHeaders()
    });

    if (!res.ok) {
        const body = await res.json().catch(() => null);
        throw new Error(body?.message ?? "Could not report file");
    }

    return await res.json();
}

export async function updateContentFlag(report: FileReport) {
    const res = await fetch(`${API_BASE}/media/report`, {
        method: "PUT",
        headers: authHeaders({"Content-Type": "application/json"}),
        body: JSON.stringify(report)
    });

    if (!res.ok) {
        const body = await res.json().catch(() => null);
        throw new Error(body?.message ?? "Could not update content flag");
    }

    return;
}

export async function deleteMediaFile(hash: string) {
    const res = await fetch(`${API_BASE}/media`, {
        method: "DELETE",
        headers: authHeaders({"Content-Type": "application/json"}),
        body: JSON.stringify({ "hash": hash })
    });

    if (!res.ok) {
        const body = await res.json().catch(() => null);
        throw new Error(body?.message ?? "Could not delete file");
    }

    return;
}
