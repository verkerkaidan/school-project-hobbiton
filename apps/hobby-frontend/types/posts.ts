import {Comment, Interaction} from "@/types/interaction";

export interface Exercise {
  title: string;
  reps: number;
  weight: number;
  rpe: number;
}

export interface MediaSet {
  relevantId: string;
  fileData: string;
  contentType: string;
  contentFlag: string;
}

export interface CompletePost {
  post: Post;
  likes: number;
  comments: Comment[];
}

export interface Post {
  id: string;
  title: string;
  description: string;
  userId: string;
  createdAt: string;
  updatedAt: string;
  hobbyType: string;
  mediaSet: MediaSet[];
  interaction: Interaction;
}

export interface Gym extends Post {
  totalDuration?: number;
  exercises?: Exercise[];
}

export interface Reading extends Post {
  bookTitle?: string;
  pagesRead?: number;
  totalPagesRead?: number;
  bookFinished?: boolean;
}

export type PostResponse = Gym | Reading;
