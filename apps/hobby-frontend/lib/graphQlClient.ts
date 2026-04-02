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

  const { data, errors } = await res.json();
  if (errors?.length) throw new Error(errors[0].message);

  return data as T;
}

export async function postCreateRequest<T>(post: CreatePost): Promise<T> {
  const endpoint: string = typeof window === 'undefined' ? MIDDLEWARE_ENDPOINT_SERVERSIDE! : MIDDLEWARE_ENDPOINT_CLIENTSIDE!;
  const res = await fetch(endpoint + "/api/create-post", {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(post),
  });

  const { data, errors } = await res.json();
  if (errors?.length) throw new Error(errors[0].message);
  return data as T;
}