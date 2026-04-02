// queries/getProfile.ts
import { gqlRequest } from "@/lib/graphQlClient";
import { Post, Gym, Reading } from "@/types/posts";
import { UserProfile, User } from "@/types/profile";

const IS_FOLLOWING_QUERY = `
  query IsFollowing($currentUserId: ID!, $targetUserId: ID!) {
    isFollowing(currentUserId: $currentUserId, targetUserId: $targetUserId)
  }
`;

const IS_BLOCKING_QUERY = `
  query IsBlocking($currentUserId: ID!, $targetUserId: ID!) {
    isBlocking(currentUserId: $currentUserId, targetUserId: $targetUserId)
  }
`;

export async function checkIsFollowing(
  currentUserId: string,
  targetUserId: string,
): Promise<boolean> {
  const data = await gqlRequest<{ isFollowing: boolean }>(IS_FOLLOWING_QUERY, {
    currentUserId,
    targetUserId,
  });
  return data.isFollowing ?? false;
}

export async function checkIsBlocking(
  currentUserId: string,
  targetUserId: string,
): Promise<boolean> {
  const data = await gqlRequest<{ isBlocking: boolean }>(IS_BLOCKING_QUERY, {
    currentUserId,
    targetUserId,
  });
  return data.isBlocking ?? false;
}

const GET_PROFILE_QUERY = `
  query GetProfile($userId: ID!) {
    getProfile(userId: $userId) {
      id
      username
      bio
      profilePictureUrl
      followerCount
      followingCount
    }
  }
`;

const GET_USER_POSTS_QUERY = `
  query GetUserPosts($userId: ID!) {
    getUserPosts(userId: $userId) {
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
      mediaSet
    }
  }
`;

const GET_FOLLOWERS_QUERY = `
  query GetFollowers($userId: ID!) {
    getFollowers(userId: $userId) {
      id
      username
      profilePictureUrl
    }
  }
`;

const GET_FOLLOWING_QUERY = `
  query GetFollowing($userId: ID!) {
    getFollowing(userId: $userId) {
      id
      username
      profilePictureUrl
    }
  }
`;

export async function getProfile(userId: string): Promise<UserProfile> {
  const data = await gqlRequest<{ getProfile: UserProfile }>(
    GET_PROFILE_QUERY,
    { userId },
  );
  return data.getProfile;
}

export async function getUserPosts(userId: string): Promise<Post[]> {
  const data = await gqlRequest<{ getUserPosts: (Gym | Reading)[] }>(
    GET_USER_POSTS_QUERY,
    { userId },
  );
  return data.getUserPosts.map((post) => ({
    ...post,
    hobbyType: post.hobbyType || "UNKNOWN",
  }));
}

export async function getFollowers(userId: string): Promise<User[]> {
  const data = await gqlRequest<{ getFollowers: User[] }>(GET_FOLLOWERS_QUERY, {
    userId,
  });
  return data.getFollowers;
}

export async function getFollowing(userId: string): Promise<User[]> {
  const data = await gqlRequest<{ getFollowing: User[] }>(GET_FOLLOWING_QUERY, {
    userId,
  });
  return data.getFollowing;
}

const TOGGLE_FOLLOW_MUTATION = `
  mutation ToggleFollow($targetUserId: ID!, $currentUserId: ID!) {
    toggleFollow(targetUserId: $targetUserId, currentUserId: $currentUserId)
  }
`;

export async function toggleFollow(
  currentUserId: string,
  targetUserId: string,
): Promise<boolean> {
  const data = await gqlRequest<{ toggleFollow: boolean }>(
    TOGGLE_FOLLOW_MUTATION,
    { targetUserId, currentUserId },
  );
  return data.toggleFollow;
}

const GET_BLOCKED_USERS_QUERY = `
  query GetBlockedUsers($userId: ID!) {
    getBlockedUsers(userId: $userId) {
      id
      username
      profilePictureUrl
    }
  }
`;

export async function getBlockedUsers(userId: string): Promise<User[]> {
  const data = await gqlRequest<{ getBlockedUsers: User[] }>(
    GET_BLOCKED_USERS_QUERY,
    { userId },
  );
  return data.getBlockedUsers ?? [];
}

const TOGGLE_BLOCK_MUTATION = `
  mutation ToggleBlock($targetUserId: ID!, $currentUserId: ID!) {
    toggleBlock(targetUserId: $targetUserId, currentUserId: $currentUserId)
  }
`;

export async function toggleBlock(
  currentUserId: string,
  targetUserId: string,
): Promise<boolean> {
  const data = await gqlRequest<{ toggleBlock: boolean }>(
    TOGGLE_BLOCK_MUTATION,
    { targetUserId, currentUserId },
  );
  return data.toggleBlock;
}

const CREATE_REPORT_MUTATION = `
  mutation CreateReport($initiatorId: ID!, $targetId: ID!, $reason: String!, $description: String) {
    createReport(initiatorId: $initiatorId, targetId: $targetId, reason: $reason, description: $description)
  }
`;

export async function createReport(
  initiatorId: string,
  targetId: string,
  reason: string,
  description: string,
): Promise<boolean> {
  const data = await gqlRequest<{ createReport: boolean }>(
    CREATE_REPORT_MUTATION,
    { initiatorId, targetId, reason, description },
  );
  return data.createReport;
}
