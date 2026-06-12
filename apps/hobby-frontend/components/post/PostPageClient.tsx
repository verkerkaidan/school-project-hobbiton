"use client";

import React, { useState } from "react";
import { Post, Gym, Reading, LinkedHobbyItem } from "@/types/posts";
import ResultDetailModal from "@/components/discover/ResultDetailModal";
import { motion, Variants } from "framer-motion";

type ResultItem =
    | { resultType: "book";     title: string; authors?: string[]; publishers?: string[]; publishedDate?: string; isbn?: string[]; pageCount?: number; languages?: string[]; coverId?: string; thumbnailUrl?: string; id?: string }
    | { resultType: "exercise"; exerciseId: string; name: string; instructions?: string[]; gifUrl?: string; bodyParts?: string[]; targetMuscles?: string[]; secondaryMuscles?: string[]; equipments?: string[] }
    | { resultType: "movie";    id: string; title: string; year?: string; rated?: string; released?: string; runtime?: string; genre?: string; director?: string; writer?: string; actors?: string; plot?: string; language?: string; country?: string; awards?: string; poster?: string; imdbRating?: string; mediaType: string };

interface Props {
    post: Post;
    author: { id: string; username: string; profilePicture?: unknown | Record<string, unknown> | null } | null;
    isGym: boolean;
    isReading: boolean;
    gymPost?: Gym;
    readingPost?: Reading;
    children: React.ReactNode;
}

const containerVariants: Variants = {
    hidden: { opacity: 0 },
    show: {
        opacity: 1,
        transition: { staggerChildren: 0.1 }
    }
};

const itemVariants: Variants = {
    hidden: { opacity: 0, y: 20 },
    show: { opacity: 1, y: 0, transition: { type: "spring", stiffness: 100, damping: 15 } }
};

export default function PostPageClient({ post, isGym, isReading, gymPost, readingPost, children }: Props) {
    console.log("=== POSTPAGECLIENT PROPS ===");
    console.log("De basis post:", post);
    console.log("Booleans -> isGym:", isGym, "| isReading:", isReading);
    console.log("Gecaste gymPost:", gymPost);
    console.log("Gecaste readingPost:", readingPost);
    const childArray = Array.isArray(children) ? children : [children];
    const [slider, authorCard, interaction] = childArray;

    const [selectedResult, setSelectedResult] = useState<ResultItem | null>(null);
    const [loadingItem, setLoadingItem] = useState<string | null>(null);

    const hobbySlug = post.hobbyType?.toLowerCase() as "gym" | "reading" | "other";

    const handleLinkedItemClick = async (item: LinkedHobbyItem) => {
        const hobbyType = item.hobbyType?.toUpperCase() ?? "";
        const apiType =
            hobbyType === "GYM" || hobbyType === "EXERCISE" ? "EXERCISE" : hobbyType === "READING" || hobbyType === "BOOK" ? "BOOK" : "MOVIE";
        const resultType = apiType === "EXERCISE" ? "exercise" : apiType === "BOOK" ? "book" : "movie";
        const endpoint = process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE;
        if (!endpoint) throw new Error("NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE is not set");
        setLoadingItem(item.externalId);

        try {
            const res = await fetch(`${endpoint}/api/library/${apiType}/${item.externalId}`);
            const json = await res.json();
            const data = json?.data ?? json;

            if (resultType === "book") {
                setSelectedResult({ resultType: "book", title: data.title ?? item.displayName, ...data, thumbnailUrl: data.thumbnailUrl ?? item.imageUrl });
            } else if (resultType === "exercise") {
                setSelectedResult({ resultType: "exercise", exerciseId: item.externalId, name: data.name ?? item.displayName, ...data, gifUrl: data.gifUrl ?? item.imageUrl });
            } else {
                setSelectedResult({ resultType: "movie", id: item.externalId, title: data.title ?? item.displayName, mediaType: data.mediaType ?? "Movie", ...data, poster: data.poster ?? item.imageUrl });
            }
        } catch {
            if (resultType === "book") {
                setSelectedResult({ resultType: "book", title: item.displayName, thumbnailUrl: item.imageUrl });
            } else if (resultType === "exercise") {
                setSelectedResult({ resultType: "exercise", exerciseId: item.externalId, name: item.displayName, gifUrl: item.imageUrl });
            } else {
                setSelectedResult({ resultType: "movie", id: item.externalId, title: item.displayName, poster: item.imageUrl, mediaType: "Movie" });
            }
        } finally {
            setLoadingItem(null);
        }
    };

    const linkedBadgeClass = (type: string) => {
        const t = type?.toUpperCase();
        if (t === "BOOK")     return "text-[11px] px-2.5 py-1 rounded-md font-bold bg-blue-500/10 text-blue-400 border border-blue-500/20";
        if (t === "EXERCISE") return "text-[11px] px-2.5 py-1 rounded-md font-bold bg-purple-500/10 text-purple-400 border border-purple-500/20";
        return "text-[11px] px-2.5 py-1 rounded-md font-bold bg-pink-500/10 text-pink-400 border border-pink-500/20";
    };

    // Veilige typecasts via unknown om ESLint happy te houden
    const gymSource = post.hobbyType === "GYM" ? (post as unknown as Gym) : undefined;
    const readingSource = post.hobbyType === "READING" ? (post as unknown as Reading) : undefined;

    const resolvedExercises = gymPost?.exercises || gymSource?.exercises;
    const resolvedBookTitle = readingPost?.bookTitle || readingSource?.bookTitle;
    const resolvedPagesRead = readingPost?.pagesRead ?? readingSource?.pagesRead;
    const resolvedTotalPages = readingPost?.totalPagesRead ?? readingSource?.totalPagesRead;
    const resolvedBookFinished = readingPost?.bookFinished || readingSource?.bookFinished;

    return (
        <motion.main
            className="max-w-7xl mx-auto px-4 py-8 md:py-12"
            variants={containerVariants}
            initial="hidden"
            animate="show"
        >
            <div className="grid grid-cols-1 lg:grid-cols-12 gap-8 items-start">

                {/* LEFT COLUMN */}
                <div className="lg:col-span-8 flex flex-col gap-6">

                    {post.mediaSet?.length > 0 && (
                        <motion.div variants={itemVariants} className="glass-card rounded-3xl overflow-hidden p-2 shadow-xl">
                            {slider}
                        </motion.div>
                    )}

                    <motion.div variants={itemVariants} className="flex items-center justify-between p-4 glass-card rounded-2xl">
                        {authorCard}
                    </motion.div>

                    <motion.div variants={itemVariants} className="space-y-4 p-6 glass-card rounded-2xl">
                        <div className="flex items-center justify-between">
                            <span className={`inline-flex items-center gap-2 px-3 py-1 rounded-full text-xs font-semibold bg-white/5 border border-white/10 uppercase tracking-wider`}>
                                <span>{hobbySlug === "gym" ? "🏋️" : hobbySlug === "reading" ? "📚" : "✨"}</span>
                                <span className="opacity-80">{post.hobbyType}</span>
                            </span>
                        </div>

                        <h1 className="text-2xl md:text-4xl font-extrabold tracking-tight text-white">
                            {post.title}
                        </h1>

                        <p className="text-sm md:text-base opacity-70 leading-relaxed font-normal whitespace-pre-wrap">
                            {post.description}
                        </p>
                    </motion.div>

                    {/* EXERCISE DATA INTEGRATION */}
                    {(isGym || resolvedExercises) && resolvedExercises && resolvedExercises.length > 0 && (
                        <motion.section variants={itemVariants} className="p-6 glass-card rounded-2xl space-y-4">
                            <p className="text-xs font-bold tracking-widest uppercase opacity-40">Exercises Tracked</p>
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                                {resolvedExercises.map((ex, i) => (
                                    <div key={i} className="flex flex-col justify-between p-4 rounded-xl bg-white/[0.02] border border-white/5 hover:border-white/10 transition-colors">
                                        <span className="font-semibold text-white mb-2">{ex.title}</span>
                                        <span className="text-xs opacity-60 font-mono bg-white/5 px-2 py-1 rounded-md w-fit">
                                            {ex.reps} reps · {ex.weight}kg · RPE {ex.rpe}
                                        </span>
                                    </div>
                                ))}
                            </div>
                        </motion.section>
                    )}

                    {/* READING DATA INTEGRATION */}
                    {(isReading || resolvedBookTitle) && (
                        <motion.section variants={itemVariants} className="p-6 glass-card rounded-2xl flex flex-col sm:flex-row sm:items-center justify-between gap-4">
                            <div className="flex items-center gap-4">
                                <div className="w-12 h-12 rounded-xl bg-blue-500/10 border border-blue-500/20 flex items-center justify-center text-xl shadow-inner">📚</div>
                                <div>
                                    <p className="text-xs font-bold tracking-widest uppercase opacity-40 mb-0.5">Currently Reading</p>
                                    <span className="font-bold text-white text-lg">{resolvedBookTitle}</span>
                                    end</div>
                            </div>
                            <div className="flex items-center gap-3">
                                <span className="text-sm font-mono opacity-80 bg-white/5 px-3 py-1.5 rounded-xl border border-white/5">
                                    {resolvedPagesRead} / {resolvedTotalPages} pages
                                </span>
                                {resolvedBookFinished && (
                                    <span className="text-xs font-bold uppercase tracking-wider bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 px-3 py-1.5 rounded-xl">
                                        Finished ✓
                                    </span>
                                )}
                            </div>
                        </motion.section>
                    )}
                </div>

                {/* RIGHT COLUMN (SIDEBAR) */}
                <div className="lg:col-span-4 lg:sticky lg:top-8 flex flex-col gap-6">
                    {post.linkedItems?.length > 0 && (
                        <motion.aside variants={itemVariants} className="glass-card rounded-2xl p-5 space-y-4">
                            <p className="text-xs font-bold tracking-widest uppercase opacity-40">Linked References</p>
                            <div className="space-y-3"> {/* Vergroot van space-y-2 naar space-y-3 */}
                                {post.linkedItems.map((item) => {
                                    const isLoading = loadingItem === item.externalId;
                                    return (
                                        <button
                                            key={item.hobbyId ?? item.externalId}
                                            onClick={() => handleLinkedItemClick(item)}
                                            disabled={isLoading}
                                            className="w-full flex items-center gap-4 p-4 rounded-2xl bg-white/[0.02] border border-white/5 hover:border-white/10 hover:bg-white/[0.04] transition-all text-left group disabled:opacity-50" // Vergroot van p-3/rounded-xl naar p-4/rounded-2xl
                                        >
                                            {item.imageUrl ? (
                                                <img src={item.imageUrl} alt={item.displayName} className="w-16 h-16 rounded-xl object-cover border border-white/10 group-hover:scale-105 transition-transform" /> // Vergroot van w-12 h-12 naar w-16 h-16
                                            ) : (
                                                <div className="w-16 h-16 rounded-xl flex items-center justify-center text-2xl border border-white/5 shadow-inner" style={{ background: "rgba(255,255,255,0.03)" }}> {/* Vergroot van w-12 h-12 naar w-16 h-16 en tekst vergroot */}
                                                    {item.hobbyType === "BOOK" ? "📚" : item.hobbyType === "EXERCISE" ? "🏋️" : "🎬"}
                                                </div>
                                            )}
                                            <div className="flex-1 min-w-0 space-y-1.5">
                                                <p className="text-base font-semibold truncate text-white group-hover:text-[var(--color-brand-light)] transition-colors">{item.displayName}</p> {/* Vergroot van text-sm naar text-base */}
                                                <span className={linkedBadgeClass(item.hobbyType)}>{item.hobbyType}</span>
                                            </div>
                                            <span className="opacity-0 group-hover:opacity-100 transition-opacity text-xs pr-1">⚡</span>
                                        </button>
                                    );
                                })}
                            </div>
                        </motion.aside>
                    )}
                </div>
            </div>

            {/* COMMENTS / INTERACTION SECTION */}
            <motion.div variants={itemVariants} className="mt-8 pt-8 border-t border-white/5">
                {interaction}
            </motion.div>

            {selectedResult && (
                <ResultDetailModal result={selectedResult} onClose={() => setSelectedResult(null)} />
            )}
        </motion.main>
    );
}