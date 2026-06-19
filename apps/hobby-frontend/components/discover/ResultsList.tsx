"use client"

import {motion} from "framer-motion";
import {ProfileSummary} from "@/lib/adminClient";
import {Result} from "postcss";
import {useRouter} from "next/navigation";

export type BookResult = {
    id?: string;
    title: string;
    authors?: string[];
    publishers?: string[];
    publishedDate?: string;
    isbn?: string[];
    pageCount?: number;
    languages?: string[];
    coverId?: string;
    thumbnailUrl?: string;
    resultType: "book";
};
export type ExerciseResult = {
    exerciseId: string;
    name: string;
    instructions?: string[];
    gifUrl?: string;
    bodyParts?: string[];
    targetMuscles?: string[];
    secondaryMuscles?: string[];
    equipments?: string[];
    resultType: "exercise";
};
export type MovieResult = {
    id: string;
    title: string;
    year?: string;
    rated?: string;
    released?: string;
    runtime?: string;
    genre?: string;
    director?: string;
    writer?: string;
    actors?: string;
    plot?: string;
    language?: string;
    country?: string;
    awards?: string;
    poster?: string;
    imdbRating?: string;
    mediaType: string;
    resultType: "movie";
};

export type UserResult = ProfileSummary & {
    resultType: "user";
}
export type ResultItem = BookResult | ExerciseResult | MovieResult | UserResult;

export default function ResultsList({
                                        results,
                                        loading,
                                        error,
                                        onBackAction,
                                        onSelectAction,
                                        onAddAction,
                                        mode = "discover",
                                        showBackButton = true, // Nieuwe prop met default op true
                                    }: {
    results: ResultItem[];
    loading: boolean;
    error: string;
    onBackAction?: () => void; // Nu optioneel gemaakt
    onSelectAction?: (result: ResultItem) => void;
    onAddAction?: (result: ResultItem) => void;
    mode?: "discover" | "select" | "user";
    showBackButton?: boolean; // Type toegevoegd
}) {
    const router = useRouter();
    const handleClick = (item: ResultItem) => {
        if (mode === "select") {
            onAddAction?.(item);
        } else if (mode === "user" && item.resultType === "user") {
            router.push(`/profile/${item.username}`)
        } else {
            onSelectAction?.(item);
        }
    };

    const getTitle = (item: ResultItem) => {
        if (item.resultType === "user") return item.username;
        if (item.resultType === "exercise") return item.name;
        return item.title;
    };

    const getImage = (item: ResultItem) => {
        if (item.resultType === "book") return item.thumbnailUrl;
        if (item.resultType === "exercise") return item.gifUrl;
        if (item.resultType === "movie") return item.poster;
        if (item.resultType === "user") {
            return item.profilePicture
            ? `data:${item.profilePicture.contentType};base64,${item.profilePicture.fileData}`
            : "/jpeg.png";
        }
        return undefined;
    };

    return (
        <motion.div
            className="flex flex-col items-center mt-6 w-full max-w-2xl" /* mt-12 verlaagd naar mt-6 voor betere modal layout */
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4 }}
        >
            {/* Alleen renderen als showBackButton true is */}
            {showBackButton && (
                <button
                    type="button"
                    className="self-start text-brand underline text-sm mb-4"
                    onClick={onBackAction}
                >
                    ← Back
                </button>
            )}

            <h3 className="text-xl font-bold mb-4 self-start">Results</h3>

            {loading && <div className="py-4 opacity-60">Loading...</div>}
            {error && <div className="text-red-500 py-2 text-sm">{error}</div>}

            <ul className="w-full">
                {results.map((item, idx) => (
                    <li
                        key={(item as any).id || (item as any).exerciseId || idx}
                        className="glass-card px-6 py-4 rounded-xl mb-4 flex gap-4 items-center cursor-pointer hover:border-brand"
                        onClick={() => handleClick(item)}
                    >
                        {getImage(item) ? (
                            <img
                                src={getImage(item)}
                                className="w-16 h-16 object-cover rounded"
                                alt=""
                            />
                        ) : (
                            <div className="w-16 h-16 bg-white/10 rounded flex items-center justify-content" />
                        )}

                        <div>
                            <div className="font-semibold">{getTitle(item)}</div>

                            {item.resultType === "book" && item.authors && (
                                <div className="text-sm text-gray-500">
                                    {item.authors.join(", ")}
                                </div>
                            )}

                            {item.resultType === "movie" && item.year && (
                                <div className="text-sm text-gray-500">{item.year}</div>
                            )}

                            {item.resultType === "exercise" && item.targetMuscles && (
                                <div className="text-sm text-gray-500">
                                    {item.targetMuscles.join(", ")}
                                </div>
                            )}
                        </div>
                    </li>
                ))}
            </ul>
        </motion.div>
    );
}