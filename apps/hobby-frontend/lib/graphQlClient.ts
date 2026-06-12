import { CreatePost } from "@/types/interfaces";

// lib/graphqlClient.ts
const MIDDLEWARE_ENDPOINT_SERVERSIDE = process.env.NEXT_PUBLIC_MIDDLEWARE_URL_SERVERSIDE;
const MIDDLEWARE_ENDPOINT_CLIENTSIDE = process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE;

export async function gqlRequest<T>(query: string, variables?: Record<string, unknown>): Promise<T> {
  const endpoint: string = typeof window === 'undefined' ? MIDDLEWARE_ENDPOINT_SERVERSIDE! : MIDDLEWARE_ENDPOINT_CLIENTSIDE!;
  const res = await fetch(endpoint + "/api/graphql", {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ query, variables }),
  });

  const json = await res.json();

  console.log("=== RUWE GRAPHQL BACKEND RESPONS ===");
  console.log(JSON.stringify(json, null, 2));

  const { data, errors } = json;
  if (errors?.length) {
    console.error("GraphQL errors:", JSON.stringify(errors, null, 2));
    throw new Error(errors[0].message);
  }

  return data as T;
}

export async function postCreateRequest<T>(post: CreatePost): Promise<T> {
  const endpoint: string = typeof window === 'undefined' ? MIDDLEWARE_ENDPOINT_SERVERSIDE! : MIDDLEWARE_ENDPOINT_CLIENTSIDE!;
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

  console.log("POST JSON", JSON.stringify(payload, null, 2)); // verify the remap

  formData.append("data", new Blob([JSON.stringify(payload)], { type: "application/json" }));
  mediaInstanceDTOSet.forEach((file) => formData.append("files[]", file));

  const res = await fetch(endpoint + "/api/create-post", {
    method: 'POST',
    body: formData,
  });

  const { data, errors } = await res.json();
  if (errors?.length) throw new Error(errors[0].message);
  return data as T;
}