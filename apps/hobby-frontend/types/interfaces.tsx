import { Exercise } from "./posts";
import {LinkedItem} from "@/types/LinkedItem";
import HobbyType from "@/types/enums";

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
  linkedItems?: LinkedItem[];
  type: HobbyType;
}

export interface CreateGymPost extends BaseCreatePost {
  type: HobbyType.GYM
  exercises: Exercise[];
}

export interface CreateReadingPost extends BaseCreatePost {
  bookTitle: string;
  type: HobbyType.READING;
  pagesRead: number;
  totalPagesRead: number;
  bookFinished: boolean;
}

export interface CreateGenericPost extends BaseCreatePost {
  type: HobbyType.OTHER;
}

export type CreatePost =
    | CreateGymPost
    | CreateReadingPost
    | CreateGenericPost;