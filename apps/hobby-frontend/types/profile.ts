export interface UserProfile {
  id: string;
  username: string;
  bio: string;
  profilePictureUrl: string;
  followerCount: number;
  followingCount: number;
}

export interface User {
  id: string;
  username: string;
  profilePictureUrl: string;
}
