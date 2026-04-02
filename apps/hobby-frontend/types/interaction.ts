import {User} from "@/types/profile";

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

export interface CompleteComment {
    userData: User,
    commentData: Comment
}