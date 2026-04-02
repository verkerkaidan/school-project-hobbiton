export interface Exercise {
  title: string;
  reps: number;
  weight: number;
  rpe: number;
}

export interface MediaSet {
  relevantId: string;
  fileData: string;
  fileThumb: string;
  contentType: string;
  contentFlag: string;
}

export interface CompletePost {
  post: Post;
  likes: number;
  comments: Comment[];
}

export interface Interaction {
  likes: number;
  comments: Comment[];
}

export interface Comment {
  id: string;
  referenceId: string;
  userId: string;
  comment: string;
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
