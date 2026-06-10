export type LinkedItem = {
    type: "BOOK" | "EXERCISE" | "MOVIE";
    title: string;
    imageUrl?: string;
    externalId?: string;
};