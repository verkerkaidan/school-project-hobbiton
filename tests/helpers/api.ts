import { expect, type APIRequestContext, type BrowserContext } from '@playwright/test';

export interface TestCredentials {
  username: string;
  email: string;
  password: string;
  userId?: string;
  token?: string;
}

export interface MediaItem {
  relevantId: string;
  fileData: string;
  contentType: string;
  contentFlag: 'SAFE' | 'RISK' | 'UNASSIGNED';
}

export interface InteractionItem {
  likes: number;
  comments: {
    id: string;
    referenceId: string;
    userId: string;
    comment: string;
  }[];
}

export interface PostWithMedia {
  id: string;
  title: string;
  description: string;
  userId: string;
  mediaSet: MediaItem[];
  interaction: InteractionItem;
}

export interface ReportedFile {
  hash: string;
  file: string;
  contentType: string;
  contentFlag: 'SAFE' | 'RISK' | 'UNASSIGNED';
}

const GATEWAY_URL = process.env.GATEWAY_URL || 'http://localhost:23738';
const FRONTEND_URL = process.env.PLAYWRIGHT_BASE_URL || 'http://localhost:19293';
const AUTH_URL = process.env.NEXT_PUBLIC_AUTHENTICATION_URL || process.env.AUTHENTICATION_URL || 'http://localhost:21525';

export function adminCredentials(): TestCredentials {
  return {
    username: process.env.ADMIN_TEST_USER || 'admin',
    email: process.env.ADMIN_TEST_EMAIL || process.env.ADMIN_TEST_USER || 'admin@localhost',
    password: process.env.ADMIN_TEST_PASSWORD || 'AdminPass123!',
  };
}

export async function authCookieValue(context: BrowserContext, name: string): Promise<string> {
  const cookie = (await context.cookies()).find((entry) => entry.name === name);
  expect(cookie, `Expected auth cookie "${name}" to be set`).toBeTruthy();
  return cookie!.value;
}

export async function graphQl<T>(
  request: APIRequestContext,
  token: string,
  query: string,
  variables?: Record<string, unknown>,
): Promise<T> {
  const res = await request.post(`${GATEWAY_URL}/api/graphql`, {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    data: { query, variables },
  });

  expect(res.ok(), await res.text()).toBeTruthy();
  const body = await res.json();
  expect(body.errors ?? [], JSON.stringify(body.errors ?? [])).toHaveLength(0);
  return body.data as T;
}

export async function getUserPosts(
  request: APIRequestContext,
  token: string,
  userId: string,
): Promise<PostWithMedia[]> {
  const data = await graphQl<{ getUserPosts: PostWithMedia[] }>(
    request,
    token,
    `
      query GetUserPosts($userId: ID!) {
        getUserPosts(userId: $userId) {
          id
          title
          description
          userId
          mediaSet {
            relevantId
            fileData
            contentType
            contentFlag
          }
          interaction {
            likes
            comments {
              id
              referenceId
              userId
              comment
            }
          }
        }
      }
    `,
    { userId },
  );

  return data.getUserPosts;
}

export async function getPostByTitle(
  request: APIRequestContext,
  token: string,
  userId: string,
  title: string,
): Promise<PostWithMedia> {
  await expect
    .poll(async () => {
      const posts = await getUserPosts(request, token, userId);
      return posts.find((post) => post.title === title);
    }, { timeout: 15000 })
    .toBeTruthy();

  const posts = await getUserPosts(request, token, userId);
  return posts.find((post) => post.title === title)!;
}

export async function getPostById(
  request: APIRequestContext,
  token: string,
  postId: string,
): Promise<PostWithMedia> {
  const data = await graphQl<{ getPostById: PostWithMedia }>(
    request,
    token,
    `
      query GetPostById($id: ID!) {
        getPostById(id: $id) {
          id
          title
          description
          userId
          mediaSet {
            relevantId
            fileData
            contentType
            contentFlag
          }
          interaction {
            likes
            comments {
              id
              referenceId
              userId
              comment
            }
          }
        }
      }
    `,
    { id: postId },
  );

  return data.getPostById;
}

export async function reportMediaFile(
  request: APIRequestContext,
  token: string,
  userId: string,
  mediaInstanceId: string,
): Promise<void> {
  const res = await request.post(`${GATEWAY_URL}/api/media/report/${userId}/${mediaInstanceId}`, {
    headers: { Authorization: `Bearer ${token}` },
  });

  expect(res.ok(), await res.text()).toBeTruthy();
}

export async function getReportedFiles(
  request: APIRequestContext,
  token: string,
): Promise<ReportedFile[]> {
  const data = await graphQl<{ getReportedFiles: ReportedFile[] }>(
    request,
    token,
    `
      query GetReportedFiles {
        getReportedFiles {
          hash
          file
          contentType
          contentFlag
        }
      }
    `,
  );

  return data.getReportedFiles ?? [];
}

export async function assignContentFlag(
  request: APIRequestContext,
  token: string,
  hash: string,
  contentFlag: 'SAFE' | 'RISK',
): Promise<void> {
  const res = await request.put(`${GATEWAY_URL}/api/media/report`, {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    data: {
      hash,
      contentFlag,
      note: 'E2E admin moderation note',
    },
  });

  expect(res.ok(), await res.text()).toBeTruthy();
}

export async function deleteMediaFile(
  request: APIRequestContext,
  token: string,
  hash: string,
): Promise<void> {
  const res = await request.delete(`${GATEWAY_URL}/api/media`, {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    data: { hash },
  });

  expect(res.ok(), await res.text()).toBeTruthy();
}

export async function createUserReport(
  request: APIRequestContext,
  token: string,
  initiatorId: string,
  targetId: string,
): Promise<void> {
  const data = await graphQl<{ createReport: boolean }>(
    request,
    token,
    `
      mutation CreateReport($initiatorId: ID!, $targetId: ID!, $reason: String!, $description: String) {
        createReport(
          initiatorId: $initiatorId
          targetId: $targetId
          reason: $reason
          description: $description
        )
      }
    `,
    {
      initiatorId,
      targetId,
      reason: 'INAPPROPRIATE_CONTENT',
      description: 'E2E account report for admin deletion.',
    },
  );

  expect(data.createReport).toBeTruthy();
}

export async function loginViaApi(
  request: APIRequestContext,
  context: BrowserContext,
  credentials: TestCredentials,
): Promise<{ token: string; userId: string }> {
  const res = await request.post(`${AUTH_URL}/api/auth/login`, {
    headers: { 'Content-Type': 'application/json' },
    data: {
      email: credentials.email,
      password: credentials.password,
    },
  });

  expect(res.ok(), await res.text()).toBeTruthy();
  const body = await res.json();
  expect(body.token).toBeTruthy();
  expect(body.userId).toBeTruthy();

  await context.addCookies([
    {
      name: 'token',
      value: body.token,
      url: FRONTEND_URL,
      path: '/',
    },
    {
      name: 'userId',
      value: body.userId,
      url: FRONTEND_URL,
      path: '/',
    },
    {
      name: 'email',
      value: credentials.email,
      url: FRONTEND_URL,
      path: '/',
    },
  ]);

  return { token: body.token, userId: body.userId };
}
