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

  const { mediaInstanceDTOSet, ...rest } = post;
  formData.append("data", new Blob([JSON.stringify(rest)], { type: "application/json" }));
  mediaInstanceDTOSet.forEach((file) => formData.append("files[]", file));
  console.log("POST OBJECT", rest);
  console.log("POST JSON", JSON.stringify(rest, null, 2));

  const res = await fetch(endpoint + "/api/create-post", {
    method: 'POST',
    body: formData,
  });

  const { data, errors } = await res.json();
  if (errors?.length) throw new Error(errors[0].message);
  return data as T;
}