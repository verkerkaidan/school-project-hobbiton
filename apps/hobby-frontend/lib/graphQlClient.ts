import { CreatePost } from "@/types/interfaces";

// lib/graphqlClient.ts
const MIDDLEWARE_ENDPOINT = process.env.NEXT_PUBLIC_MIDDLEWARE_URL;

export async function gqlRequest<T>(query: string, variables?: Record<string, unknown>): Promise<T> {
  const res = await fetch(MIDDLEWARE_ENDPOINT + "/api/graphql", {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ query, variables }),
  });

  const { data, errors } = await res.json();
  if (errors?.length) throw new Error(errors[0].message);

  return data as T;
}

export async function postCreateRequest<T>(post: CreatePost): Promise<T> {
  const res = await fetch(MIDDLEWARE_ENDPOINT + "/api/create-post", {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(post),
  });

  const { data, errors } = await res.json();
  if (errors?.length) throw new Error(errors[0].message);

  return data as T;
}