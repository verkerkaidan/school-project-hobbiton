import { CreatePost } from "@/types/interfaces";
import { UUID } from "node:crypto";
import { authHeaders } from "@/lib/adminClient";

// lib/graphqlClient.ts
const MIDDLEWARE_ENDPOINT_SERVERSIDE =
  process.env.NEXT_PUBLIC_MIDDLEWARE_URL_SERVERSIDE;
const MIDDLEWARE_ENDPOINT_CLIENTSIDE =
  process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE;

export async function gqlRequest<T>(
  query: string,
  variables?: Record<string, unknown>,
): Promise<T> {
  const endpoint: string =
    typeof window === "undefined"
      ? MIDDLEWARE_ENDPOINT_SERVERSIDE!
      : MIDDLEWARE_ENDPOINT_CLIENTSIDE!;
  const res = await fetch(endpoint + "/api/graphql", {
    method: "POST",
    headers: authHeaders({ "Content-Type": "application/json" }),
    body: JSON.stringify({ query, variables }),
  });

  const json = await res.json();

  const { data, errors } = json;
  if (errors?.length) {
    console.error("GraphQL errors:", JSON.stringify(errors, null, 2));
    throw new Error(errors[0].message);
  }

  return data as T;
}

export async function postCreateRequest<T>(post: CreatePost): Promise<UUID[]> {
  const endpoint: string =
    typeof window === "undefined"
      ? MIDDLEWARE_ENDPOINT_SERVERSIDE!
      : MIDDLEWARE_ENDPOINT_CLIENTSIDE!;
  const formData = new FormData();

  const { mediaInstanceDTOSet, linkedItems, ...rest } = post;

  // ← remap frontend LinkedItem fields to what the backend DTO expects
  const mappedLinkedItems = (linkedItems ?? []).map((li) => ({
    displayName: li.title,
    hobbyType: li.type,
    imageUrl: li.imageUrl,
    externalId: li.externalId,
  }));

  const payload = { ...rest, linkedItems: mappedLinkedItems };

  formData.append(
    "data",
    new Blob([JSON.stringify(payload)], { type: "application/json" }),
  );
  mediaInstanceDTOSet.forEach((file) => formData.append("files[]", file));

  const res = await fetch(endpoint + "/api/create-post", {
    method: "POST",
    headers: authHeaders(),
    body: formData,
  });

  if (!res.ok) {
    const body = await res.json().catch(() => null);
    throw new Error(body?.message ?? "Could not create post");
  }

  return (await res.json()) as UUID[];
}
