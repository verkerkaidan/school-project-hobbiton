export type PostSize = 'short' | 'medium' | 'tall';

export interface Post {
    type: string;
    title: string;
    content: string;
    image: string;
    color: string;
    href: string;
    size: PostSize;
}