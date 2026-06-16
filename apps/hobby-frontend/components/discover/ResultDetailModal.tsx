import React, { useState, useEffect } from "react";
import Image from "next/image";

import { addItem, getLists, getResolvedItems } from "@/lib/libraryClient";
import { useCurrentUser } from "@/hooks/useCurrentUser";

// Types matching the actual API response
type BookResult = {
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
type ExerciseResult = {
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
type MovieResult = {
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
type ResultItem = BookResult | ExerciseResult | MovieResult;

export default function ResultDetailModal({
                                              result,
                                              onClose,
                                          }: {
    result: ResultItem;
    onClose: () => void;
}) {
    const user = useCurrentUser();
    const userId = user?.id;
    const [lists, setLists] = useState<any[]>([]);
    const [showSave, setShowSave] = useState(false);

    // Track counts of this specific item across all user lists
    const [savedCounts, setSavedCounts] = useState<Record<string, number>>({});
    const [loadingCounts, setLoadingCounts] = useState(false);

    // Track current unique item target identifier
    const currentTargetId =
        result.resultType === "book"
            ? (result.isbn?.[0] ?? result.id)
            : result.resultType === "movie"
                ? result.id
                : result.exerciseId;

    // Safely loads folders & calculates item duplicates when save menu is triggered
    useEffect(() => {
        if (!showSave || !userId) return;

        setLoadingCounts(true);
        getLists(userId)
            .then(async (res) => {
                const data = res.data ?? res;
                const listsArray = Array.isArray(data) ? data : [];
                setLists(listsArray);

                const countsMap: Record<string, number> = {};

                // Concurrently count occurrences inside each list
                await Promise.all(
                    listsArray.map(async (list: any) => {
                        try {
                            const itemsRes = await getResolvedItems(list.id);
                            const itemsList = itemsRes.data ?? itemsRes;
                            if (Array.isArray(itemsList)) {
                                const matches = itemsList.filter((item: any) => {
                                    const itemId = item.resultType === "exercise" ? item.exerciseId : item.id;
                                    return itemId === currentTargetId;
                                });
                                countsMap[list.id] = matches.length;
                            }
                        } catch (err) {
                            console.error(`Failed loading count for list ${list.id}:`, err);
                        }
                    })
                );

                setSavedCounts(countsMap);
            })
            .catch((err) => console.error("Error loading lists:", err))
            .finally(() => setLoadingCounts(false));
    }, [showSave, userId, currentTargetId]);

    async function saveToList(listId: string) {
        const body =
            result.resultType === "book"
                ? {
                    itemType: "BOOK",
                    itemId: result.isbn?.[0] ?? result.id,
                    notes: result.title,
                }
                : result.resultType === "movie"
                    ? {
                        itemType: "MOVIE",
                        itemId: result.id,
                        notes: result.title,
                    }
                    : {
                        itemType: "EXERCISE",
                        itemId: result.exerciseId,
                        notes: result.name,
                    };

        try {
            await addItem(listId, body);
            // Optimistically increment local state counter for immediate visual feedback
            setSavedCounts(prev => ({
                ...prev,
                [listId]: (prev[listId] || 0) + 1
            }));
        } catch (err) {
            console.error("Error saving item to list:", err);
        }
    }

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4 py-4 backdrop-blur-sm animate-fade-in">
            <div
                className="glass-card rounded-2xl border border-white/20 shadow-2xl max-w-2xl w-full p-8 relative backdrop-blur-2xl max-h-[90vh] overflow-y-auto transform transition-all duration-300"
                style={{ boxShadow: "0 25px 50px -12px rgba(236,72,153,0.2)" }}
            >
                {/* Top Header Actions */}
                <div className="absolute top-4 right-4 flex items-center gap-3">
                    <button
                        className="bg-brand hover:bg-brand/90 text-white font-semibold text-sm px-4 py-2 rounded-xl transition-all shadow-md hover:shadow-brand/20 active:scale-95 flex items-center gap-1.5"
                        onClick={() => setShowSave(true)}
                    >
                        Save to List
                    </button>

                    <button
                        className="text-3xl text-gray-400 hover:text-brand hover:scale-110 transition-transform focus:outline-none leading-none"
                        onClick={onClose}
                        aria-label="Close"
                    >
                        ×
                    </button>
                </div>

                {/* BOOK DETAIL */}
                {result.resultType === "book" && (
                    <div className="space-y-6 pt-6">
                        {/* Header with image and title */}
                        <div className="flex gap-6 items-start">
                            {result.thumbnailUrl && (
                                <div className="shrink-0">
                                    <Image
                                        src={result.thumbnailUrl}
                                        alt={result.title}
                                        width={128}
                                        height={192}
                                        className="object-cover rounded-xl border border-white/20 shadow-lg"
                                        unoptimized
                                    />
                                </div>
                            )}
                            <div className="flex-1">
                                <h2 className="text-3xl font-bold mb-2 text-white">{result.title}</h2>
                                {result.authors && (
                                    <p className="text-brand mb-2 font-semibold">By {result.authors.join(", ")}</p>
                                )}
                                {result.publishedDate && (
                                    <p className="text-gray-400">Published: {result.publishedDate}</p>
                                )}
                                <div className="mt-4 flex flex-wrap gap-2">
                                    {result.pageCount && (
                                        <span className="px-3 py-1 rounded-full bg-brand/20 text-brand text-sm">
                      {result.pageCount} pages
                    </span>
                                    )}
                                    {result.languages && result.languages.length > 0 && (
                                        <span className="px-3 py-1 rounded-full bg-accent/20 text-accent text-sm">
                      {result.languages.join(", ")}
                    </span>
                                    )}
                                </div>
                            </div>
                        </div>

                        {/* Details grid */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t border-white/10">
                            {result.publishers && result.publishers.length > 0 && (
                                <div className="bg-white/5 rounded-lg p-4">
                                    <p className="text-gray-400 text-sm font-semibold mb-1">Publishers</p>
                                    <div className="text-white text-sm">
                                        {result.publishers.map((p, i) => (
                                            <div key={i}>{p}</div>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {result.isbn && result.isbn.length > 0 && (
                                <div className="bg-white/5 rounded-lg p-4">
                                    <p className="text-gray-400 text-sm font-semibold mb-1">Identifiers (ISBN)</p>
                                    <div className="flex flex-col gap-1">
                                        {result.isbn.map((code, i) => (
                                            <code key={i} className="text-white text-sm font-mono">{code}</code>
                                        ))}
                                    </div>
                                </div>
                            )}

                            {result.pageCount && (
                                <div className="bg-white/5 rounded-lg p-4">
                                    <p className="text-gray-400 text-sm font-semibold mb-1">Page count</p>
                                    <p className="text-white">{result.pageCount}</p>
                                </div>
                            )}

                            {result.languages && result.languages.length > 0 && (
                                <div className="bg-white/5 rounded-lg p-4">
                                    <p className="text-gray-400 text-sm font-semibold mb-1">Languages</p>
                                    <p className="text-white">{result.languages.join(", ")}</p>
                                </div>
                            )}

                            {result.coverId && (
                                <div className="bg-white/5 rounded-lg p-4">
                                    <p className="text-gray-400 text-sm font-semibold mb-1">Cover</p>
                                    <div className="flex items-center gap-3">
                                        <Image
                                            src={`https://covers.openlibrary.org/b/id/${result.coverId}-S.jpg`}
                                            alt="cover"
                                            width={64}
                                            height={80}
                                            className="object-cover rounded"
                                            unoptimized
                                        />
                                        <div>
                                            <p className="text-white text-sm">Cover ID: {result.coverId}</p>
                                            <a className="text-brand text-sm underline" target="_blank" rel="noreferrer" href={`https://covers.openlibrary.org/b/id/${result.coverId}-L.jpg`}>Open large cover</a>
                                        </div>
                                    </div>
                                </div>
                            )}

                            {result.id && (
                                <div className="bg-white/5 rounded-lg p-4 md:col-span-2">
                                    <p className="text-gray-400 text-sm font-semibold mb-1">OpenLibrary</p>
                                    <div className="flex items-center justify-between">
                                        <a className="text-brand underline" target="_blank" rel="noreferrer" href={`https://openlibrary.org${result.id}`}>View on OpenLibrary</a>
                                        <span className="text-white text-sm">Work ID: {result.id.replace('/works/', '')}</span>
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                )}

                {/* EXERCISE DETAIL */}
                {result.resultType === "exercise" && (
                    <div className="space-y-6 pt-6">
                        {/* Header with image and title */}
                        <div className="flex gap-6 items-start">
                            {result.gifUrl && (
                                <div className="shrink-0">
                                    <Image
                                        src={result.gifUrl}
                                        alt={result.name}
                                        width={128}
                                        height={128}
                                        className="object-cover rounded-xl border border-white/20 shadow-lg"
                                        unoptimized
                                    />
                                </div>
                            )}
                            <div className="flex-1">
                                <h2 className="text-3xl font-bold mb-3 text-white capitalize">{result.name}</h2>
                                <div className="flex flex-wrap gap-2 mb-4">
                                    {result.bodyParts && result.bodyParts.length > 0 && (
                                        <span className="px-3 py-1 rounded-full bg-brand/20 text-brand text-sm capitalize">
                      {result.bodyParts.join(", ")}
                    </span>
                                    )}
                                    {result.targetMuscles && result.targetMuscles.length > 0 && (
                                        <span className="px-3 py-1 rounded-full bg-accent/20 text-accent text-sm capitalize">
                      {result.targetMuscles.join(", ")}
                    </span>
                                    )}
                                </div>
                            </div>
                        </div>

                        {/* Instructions */}
                        {result.instructions && result.instructions.length > 0 && (
                            <div className="bg-white/5 rounded-lg p-4 border border-white/10">
                                <h3 className="text-lg font-bold mb-3 text-white flex items-center gap-2">
                                    Step-by-Step Instructions
                                </h3>
                                <ol className="space-y-2">
                                    {result.instructions.map((step, i) => (
                                        <li key={i} className="text-gray-300 text-sm flex gap-3">
                      <span className="shrink-0 w-6 h-6 rounded-full bg-brand/30 text-brand flex items-center justify-center text-xs font-bold">
                        {i + 1}
                      </span>
                                            <span>{step}</span>
                                        </li>
                                    ))}
                                </ol>
                            </div>
                        )}

                        {/* Muscles and Equipment grid */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t border-white/10">
                            {result.secondaryMuscles && result.secondaryMuscles.length > 0 && (
                                <div className="bg-white/5 rounded-lg p-4">
                                    <p className="text-gray-400 text-sm font-semibold mb-2">Secondary Muscles</p>
                                    <div className="flex flex-wrap gap-2">
                                        {result.secondaryMuscles.map((muscle, i) => (
                                            <span key={i} className="px-2 py-1 bg-white/10 text-white text-xs rounded capitalize">
                        {muscle}
                      </span>
                                        ))}
                                    </div>
                                </div>
                            )}
                            {result.equipments && result.equipments.length > 0 && (
                                <div className="bg-white/5 rounded-lg p-4">
                                    <p className="text-gray-400 text-sm font-semibold mb-2">Equipment</p>
                                    <div className="flex flex-wrap gap-2">
                                        {result.equipments.map((eq, i) => (
                                            <span key={i} className="px-2 py-1 bg-white/10 text-white text-xs rounded capitalize">
                        {eq}
                      </span>
                                        ))}
                                    </div>
                                </div>
                            )}
                        </div>
                    </div>
                )}

                {/* MOVIE DETAIL */}
                {result.resultType === "movie" && (
                    <div className="space-y-6 pt-6">
                        {/* Header with poster and title */}
                        <div className="flex gap-6 items-start">
                            {result.poster && (
                                <div className="shrink-0">
                                    <Image
                                        src={result.poster}
                                        alt={result.title}
                                        width={128}
                                        height={192}
                                        className="object-cover rounded-xl border border-white/20 shadow-lg"
                                        unoptimized
                                    />
                                </div>
                            )}
                            <div className="flex-1">
                                <h2 className="text-3xl font-bold mb-2 text-white">{result.title}</h2>
                                <p className="text-brand font-semibold mb-3">{result.mediaType}</p>

                                {/* Key info badges */}
                                <div className="flex flex-wrap gap-2 mb-4">
                                    {result.year && (
                                        <span className="px-3 py-1 rounded-full bg-brand/20 text-brand text-sm">
                      {result.year}
                    </span>
                                    )}
                                    {result.rated && (
                                        <span className="px-3 py-1 rounded-full bg-orange-500/20 text-orange-400 text-sm font-semibold">
                      {result.rated}
                    </span>
                                    )}
                                    {result.imdbRating && (
                                        <span className="px-3 py-1 rounded-full bg-yellow-500/20 text-yellow-400 text-sm font-semibold">
                      {result.imdbRating}/10
                    </span>
                                    )}
                                </div>

                                {/* Release and runtime */}
                                {result.released && (
                                    <p className="text-gray-400 text-sm mb-1">Released: {result.released}</p>
                                )}
                                {result.runtime && (
                                    <p className="text-gray-400 text-sm">Duration: {result.runtime}</p>
                                )}
                            </div>
                        </div>

                        {/* Plot */}
                        {result.plot && (
                            <div className="bg-white/5 rounded-lg p-4 border border-white/10">
                                <h3 className="font-semibold mb-2 text-white">Plot</h3>
                                <p className="text-gray-300 text-sm leading-relaxed">{result.plot}</p>
                            </div>
                        )}

                        {/* Credits and details grid */}
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t border-white/10">
                            {result.director && (
                                <div className="bg-white/5 rounded-lg p-4">
                                    <p className="text-gray-400 text-sm font-semibold mb-1">Director</p>
                                    <p className="text-white">{result.director}</p>
                                </div>
                            )}
                            {result.genre && (
                                <div className="bg-white/5 rounded-lg p-4">
                                    <p className="text-gray-400 text-sm font-semibold mb-1">Genre</p>
                                    <div className="flex flex-wrap gap-2">
                                        {result.genre.split(",").map((g, i) => (
                                            <span key={i} className="px-2 py-1 bg-brand/20 text-brand text-xs rounded">
                        {g.trim()}
                      </span>
                                        ))}
                                    </div>
                                </div>
                            )}
                            {result.writer && (
                                <div className="bg-white/5 rounded-lg p-4">
                                    <p className="text-gray-400 text-sm font-semibold mb-1">Writer</p>
                                    <p className="text-white text-sm">{result.writer}</p>
                                </div>
                            )}
                            {result.actors && (
                                <div className="bg-white/5 rounded-lg p-4">
                                    <p className="text-gray-400 text-sm font-semibold mb-1">Cast</p>
                                    <p className="text-white text-sm">{result.actors}</p>
                                </div>
                            )}
                            {result.language && (
                                <div className="bg-white/5 rounded-lg p-4">
                                    <p className="text-gray-400 text-sm font-semibold mb-1">Language</p>
                                    <p className="text-white">{result.language}</p>
                                </div>
                            )}
                            {result.country && (
                                <div className="bg-white/5 rounded-lg p-4">
                                    <p className="text-gray-400 text-sm font-semibold mb-1">Country</p>
                                    <p className="text-white">{result.country}</p>
                                </div>
                            )}
                            {result.awards && (
                                <div className="bg-white/5 rounded-lg p-4 md:col-span-2">
                                    <p className="text-gray-400 text-sm font-semibold mb-1">Awards</p>
                                    <p className="text-white">{result.awards}</p>
                                </div>
                            )}
                        </div>
                    </div>
                )}
            </div>

            {/* Save to list sliding drawer overlay with list counts */}
            {showSave && (
                <div className="fixed inset-0 bg-black/60 flex items-end justify-center z-[60] animate-fade-in">
                    <div className="absolute inset-0" onClick={() => setShowSave(false)} />
                    <div className="w-full max-w-2xl bg-zinc-900 border-t border-white/10 p-6 rounded-t-3xl shadow-2xl relative z-10 transition-transform duration-300 translate-y-0">
                        <div className="flex justify-between items-center mb-4">
                            <div>
                                <h3 className="font-bold text-lg text-white">Save to list</h3>
                                <p className="text-xs text-gray-400">Items can be added multiple times</p>
                            </div>
                            <button
                                onClick={() => setShowSave(false)}
                                className="text-sm text-gray-400 hover:text-white transition-colors bg-white/5 px-3 py-1 rounded-lg"
                            >
                                Close
                            </button>
                        </div>

                        <div className="space-y-2 max-h-[40vh] overflow-y-auto pr-1">
                            {lists.map((l) => {
                                const currentCount = savedCounts[l.id] || 0;
                                return (
                                    <button
                                        key={l.id}
                                        onClick={() => saveToList(l.id)}
                                        className="w-full p-4 bg-white/5 hover:bg-white/10 border border-white/5 rounded-xl text-left transition-all text-white font-medium flex justify-between items-center group active:scale-[0.99]"
                                    >
                                        <div className="flex items-center gap-3">
                                            <span>{l.name}</span>
                                            {loadingCounts ? (
                                                <span className="w-1.5 h-1.5 rounded-full bg-brand animate-ping" />
                                            ) : currentCount > 0 ? (
                                                <span className="text-[10px] bg-brand/20 text-brand px-2 py-0.5 rounded-md border border-brand/30 font-bold">
                                                    In list ({currentCount}x)
                                                </span>
                                            ) : null}
                                        </div>
                                        <span className="text-brand opacity-60 group-hover:opacity-100 transition-all text-sm font-semibold">
                                            + Add Item
                                        </span>
                                    </button>
                                );
                            })}
                            {lists.length === 0 && !loadingCounts && (
                                <p className="text-gray-400 text-sm py-4 text-center">No lists available.</p>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}