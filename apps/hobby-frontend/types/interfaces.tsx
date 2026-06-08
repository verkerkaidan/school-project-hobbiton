import { Exercise } from "./posts";

export interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export interface LoginForm {
  email: string;
  password: string;
}

export interface RegisterForm {
  email: string;
  password: string;
  confirmPassword: string;
  username: string;
  name: string;
}

export interface BaseCreatePost {
  title: string;
  description: string;
  totalDuration: number;
  userId: string;
  mediaInstanceDTOSet: File[];
}

export interface CreateGymPost extends BaseCreatePost {
  type: "GYM";
  excercises: Exercise[];
}

export interface CreateReadingPost extends BaseCreatePost {
  bookTitle: string;
  type: "READING";
  pagesRead: number;
  totalPagesRead: number;
  bookFinished: boolean;
}

export interface CreateOtherPost extends BaseCreatePost {
  type: "OTHER";
}

export type CreatePost = CreateGymPost | CreateReadingPost | CreateOtherPost;
