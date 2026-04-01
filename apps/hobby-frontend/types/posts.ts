// types/posts.ts
interface Exercise {
  id: string;
  title: string;
  reps: number;
  weight: number;
  rpe: number;
}

interface MediaSet {
  fileData: string;
  fileThumb: string;
  contentType: string;
  contentFlag: string;
}

interface Post {
  id: string;
  title: string;
  description: string;
  userId: string;
  createdAt: string;
  updatedAt: string;
  hobbyType: string;
  mediaSet: MediaSet[];
}

interface Gym extends Post {
  totalDuration?: number;
  exercises?: Exercise[];
}

interface Reading extends Post {
  bookTitle?: string;
  pagesRead?: number;
  totalPagesRead?: number;
  bookFinished?: boolean;
}

