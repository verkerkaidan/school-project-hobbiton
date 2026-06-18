// queries/getPosts.ts

import { gqlRequest } from "@/lib/graphQlClient";
import { PostResponse } from "@/types/posts";

const GET_PAGED_POSTS_QUERY = `
query GetPosts($page: Int, $size: Int) {
  getPosts(page: $page, size: $size) {
    id
    title
    description
    userId
    createdAt
    updatedAt
    hobbyType
    authorUsername
    ... on Gym {
      totalDuration
      exercises {
        id
        title
        reps
        weight
        rpe
      }
    }
    ... on Reading {
        bookTitle
        pagesRead
        totalPagesRead
        bookFinished
    }
    ... on Other {
        __typename
    }
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
    linkedItems {
      hobbyId
      externalId
      hobbyType
      displayName
      imageUrl
    }
  }
}
`;

export async function getPagedPosts(page: number, size: number = 16): Promise<PostResponse[]> {
  const data = await gqlRequest<{ getPosts: PostResponse[] }>(GET_PAGED_POSTS_QUERY, { page, size });
  return data.getPosts.map((post) => ({
    ...post,
    hobbyType: post.hobbyType || "UNKNOWN",
  }));
}
