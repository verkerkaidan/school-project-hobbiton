// queries/getPosts.ts

import { gqlRequest } from "@/lib/graphQlClient";
import { HobbyType } from "@/types/enums";
import { Post, Gym, Reading, PostResponse } from "@/types/posts";

const GET_POSTS_QUERY = `
query {
  getPosts {
    id
    title
    description
    userId
    createdAt
    updatedAt
    hobbyType
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
`;


export async function getAllPosts(): Promise<Post[]> {
  const data = await gqlRequest<{ getPosts: PostResponse[] }>(GET_POSTS_QUERY);

  return data.getPosts.map((post) => ({
    ...post,
    hobbyType: post.hobbyType || "UNKNOWN", // fallback if GraphQL doesn’t send it
  }));
}
