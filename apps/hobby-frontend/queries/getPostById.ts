// queries/getPostById.ts

import { gqlRequest } from "@/lib/graphQlClient";
import { Post, PostResponse } from "@/types/posts";

const GET_POST_BY_ID_QUERY = `
query GetPostById($id: ID!) {
  getPostById(id: $id) {
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

export async function getPostById(id: string): Promise<Post> {
    const data = await gqlRequest<{ getPostById: PostResponse }>(
        GET_POST_BY_ID_QUERY,
        { id }
    );

    return {
        ...data.getPostById,
        hobbyType: data.getPostById.hobbyType || "UNKNOWN",
    };
}