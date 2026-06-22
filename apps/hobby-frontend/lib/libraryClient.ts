import { authHeaders } from "@/lib/adminClient";

const GATEWAY_URL =
    process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE ??
    "http://localhost:23738";

const API_BASE = `${GATEWAY_URL}/api/library`;

export async function getLists(userId: string) {
    const res = await fetch(`${API_BASE}/users/${userId}/lists`, {
        headers: authHeaders(),
    });
    if (!res.ok) throw new Error("Failed to fetch lists");
    return res.json();
}

export async function createList(
    userId: string,
    name: string,
    description?: string
) {
    const res = await fetch(`${API_BASE}/users/${userId}/lists`, {
        method: "POST",
        headers: authHeaders({ "Content-Type": "application/json" }),
        body: JSON.stringify({ name, description }),
    });

    if (!res.ok) throw new Error("Failed to create list");
    return res.json();
}

// NEW: Update list name and/or description
export async function updateList(
    userId: string,
    listId: string,
    name?: string,
    description?: string
) {
    const res = await fetch(`${API_BASE}/users/${userId}/lists/${listId}`, {
        method: "PATCH",
        headers: authHeaders({ "Content-Type": "application/json" }),
        body: JSON.stringify({ name, description }),
    });

    if (!res.ok) throw new Error("Failed to update list");
    return res.json();
}

export async function deleteList(userId: string, listId: string) {
    const res = await fetch(
        `${API_BASE}/users/${userId}/lists/${listId}`,
        { method: "DELETE", headers: authHeaders() }
    );

    if (!res.ok) throw new Error("Failed to delete list");
    return res.json();
}

export async function getResolvedItems(listId: string) {
    const res = await fetch(
        `${API_BASE}/lists/${listId}/items/resolved`,
        { headers: authHeaders() }
    );

    if (!res.ok) throw new Error("Failed to fetch items");
    return res.json();
}

export async function addItem(listId: string, body: any) {
    const res = await fetch(
        `${API_BASE}/lists/${listId}/items`,
        {
            method: "POST",
            headers: authHeaders({ "Content-Type": "application/json" }),
            body: JSON.stringify(body),
        }
    );

    if (!res.ok) throw new Error("Failed to add item");
    return res.json();
}

export async function removeItem(listId: string, itemId: string) {
    const res = await fetch(
        `${API_BASE}/lists/${listId}/items/${itemId}`,
        { method: "DELETE", headers: authHeaders() }
    );

    if (!res.ok) throw new Error("Failed to remove item");
    return res.json();
}

export async function reorderItems(listId: string, itemIds: string[]) {
    const res = await fetch(`${API_BASE}/lists/${listId}/items/reorder`, {
        method: "PUT",
        headers: authHeaders({ "Content-Type": "application/json" }),
        body: JSON.stringify({ itemIds }),
    });

    if (!res.ok) throw new Error("Failed to reorder items");
    return res.json();
}

export async function searchLibrary(queryType: string, query: string): Promise<any> {
    // Adjust port if needed; default Spring Boot is 8080
    const res = await fetch(
        `${process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE}/api/library/search`,
        {
            method: "POST",
            headers: authHeaders({"Content-Type": "application/json"}),
            body: JSON.stringify({type: queryType, query, maxResults: 10}),
        }
    );

    if (!res.ok) throw new Error("Failed to fetch results");
    const data = await res.json();
    if (!data.success) throw new Error(data.message || "Unknown error");

    return data;
}