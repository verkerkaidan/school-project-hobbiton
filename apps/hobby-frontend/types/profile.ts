import {MediaSet} from "@/types/posts";

export interface User {
  id: string;
  username: string;
  bio?: string;
  profilePicture?: MediaSet;
  followerCount?: number;
  followingCount?: number;
}
