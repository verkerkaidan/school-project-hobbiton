import { gqlRequest } from "@/lib/graphQlClient";
import { PostResponse } from "@/types/posts";

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

export async function getPostById(id: string): Promise<PostResponse> {
    const data = await gqlRequest<{ getPostById: PostResponse }>(
        GET_POST_BY_ID_QUERY,
        { id }
    );

    if (!data?.getPostById) return null as any;

    return {
        ...data.getPostById,
        hobbyType: data.getPostById.hobbyType || "UNKNOWN",
    };
}