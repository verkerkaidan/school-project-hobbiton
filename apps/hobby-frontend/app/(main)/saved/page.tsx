"use client";

import { useEffect, useState } from "react";
import { useCurrentUser } from "@/hooks/useCurrentUser";
import {
    getLists,
    createList,
    deleteList,
    getResolvedItems,
    removeItem,
    reorderItems,
    addItem,
} from "@/lib/libraryClient";
import SearchBar from "@/components/discover/SearchBar";
import ResultsList from "@/components/discover/ResultsList";
import ResultDetailModal from "@/components/discover/ResultDetailModal"; // Adjust path if needed
import { AnimatePresence, motion } from "framer-motion";

type List = {
    id: string;
    name: string;
    description?: string;
};

type SavedItem = (
    | { resultType: "book"; id: string; title: string; authors?: string[]; }
    | { resultType: "movie"; id: string; title: string; }
    | { resultType: "exercise"; exerciseId: string; name: string; }
    ) & { notes?: string };

type QueryType = "book" | "movie" | "exercise";

const QUERY_TYPES: { value: QueryType; label: string; icon: string }[] = [
    { value: "book",     label: "Book",     icon: "📚" },
    { value: "movie",    label: "Movie",    icon: "🎬" },
    { value: "exercise", label: "Exercise", icon: "🏋️" },
];

export default function SavedPage() {
    const user = useCurrentUser();
    const userId = user?.id;

    const [lists, setLists] = useState<List[]>([]);
    const [activeList, setActiveList] = useState<List | null>(null);
    const [items, setItems] = useState<SavedItem[]>([]);

    const [newListName, setNewListName] = useState("");
    const [newListDesc, setNewListDesc] = useState("");

    // Search states for inline additions
    const [searchOpen, setSearchOpen] = useState(false);
    const [queryType, setQueryType] = useState<QueryType>("book");
    const [searchResults, setSearchResults] = useState<any[]>([]);
    const [searchLoading, setSearchLoading] = useState(false);
    const [searchError, setSearchError] = useState("");
    const [hasSearched, setHasSearched] = useState(false);

    // Modal state for viewing item deep details
    const [selectedModalItem, setSelectedModalItem] = useState<any | null>(null);

    useEffect(() => {
        if (!userId) return;
        getLists(userId)
            .then((res) => setLists(res.data ?? res))
            .catch(console.error);
    }, [userId]);

    useEffect(() => {
        if (!activeList) return;
        getResolvedItems(activeList.id)
            .then((res) => setItems(res.data ?? res))
            .catch(console.error);

        // Reset search panel state when switching lists
        setSearchOpen(false);
        setSearchResults([]);
        setHasSearched(false);
    }, [activeList]);

    async function handleCreateList(e: React.FormEvent) {
        e.preventDefault();
        if (!userId || !newListName.trim()) return;

        try {
            const res = await createList(userId, newListName.trim(), newListDesc.trim() || undefined);
            const newList = res.data ?? res;
            setLists((prev) => [newList, ...prev]);
            setNewListName("");
            setNewListDesc("");
            setActiveList(newList);
        } catch (err) {
            console.error("Failed to create list:", err);
        }
    }

    async function handleDeleteList(listId: string) {
        try {
            await deleteList(userId!, listId);
            setLists((prev) => prev.filter((l) => l.id !== listId));
            if (activeList?.id === listId) {
                setActiveList(null);
                setItems([]);
            }
        } catch (err) {
            console.error("Failed to delete list:", err);
        }
    }

    // Inline Search Functionality
    const handleSearch = async (query: string) => {
        if (!queryType) return;
        setSearchLoading(true);
        setSearchError("");
        setHasSearched(true);
        try {
            const res = await fetch(
                `${process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE}/api/library/search`,
                {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ type: queryType.toUpperCase(), query, maxResults: 10 }),
                }
            );
            const data = await res.json();
            if (!data.success) throw new Error(data.message || "Search failed");
            setSearchResults(data.data || []);
        } catch (e: unknown) {
            setSearchError(e instanceof Error ? e.message : "Error searching");
        } finally {
            setSearchLoading(false);
        }
    };

    const handleQueryTypeChange = (type: QueryType) => {
        setQueryType(type);
        setSearchResults([]);
        setHasSearched(false);
        setSearchError("");
    };

    async function handleAddItemFromSearch(item: any) {
        if (!activeList) return;

        const body =
            item.resultType === "book"
                ? {
                    itemType: "BOOK",
                    itemId: item.isbn?.[0] ?? item.id,
                    notes: item.title,
                }
                : item.resultType === "movie"
                    ? {
                        itemType: "MOVIE",
                        itemId: item.id,
                        notes: item.title,
                    }
                    : {
                        itemType: "EXERCISE",
                        itemId: item.exerciseId,
                        notes: item.name,
                    };

        try {
            await addItem(activeList.id, body);
            // Refresh the list to catch detailed attributes automatically
            const res = await getResolvedItems(activeList.id);
            setItems(res.data ?? res);
        } catch (err) {
            console.error("Failed to add item to list:", err);
        }
    }

    async function handleRemoveItem(item: SavedItem) {
        if (!activeList) return;
        const itemId = item.resultType === "exercise" ? item.exerciseId : item.id;

        try {
            await removeItem(activeList.id, itemId);
            setItems((prev) => prev.filter((i) => (i.resultType === "exercise" ? i.exerciseId : i.id) !== itemId));
        } catch (err) {
            console.error("Failed to remove item:", err);
        }
    }

    async function moveItem(index: number, direction: "up" | "down") {
        if (!activeList) return;
        const targetIndex = direction === "up" ? index - 1 : index + 1;
        if (targetIndex < 0 || targetIndex >= items.length) return;

        const updatedItems = [...items];
        const [movedItem] = updatedItems.splice(index, 1);
        updatedItems.splice(targetIndex, 0, movedItem);
        setItems(updatedItems);

        const orderedIds = updatedItems.map((i) => (i.resultType === "exercise" ? i.exerciseId : i.id));
        try {
            await reorderItems(activeList.id, orderedIds);
        } catch (err) {
            console.error("Failed to reorder items:", err);
        }
    }

    if (!user) return null;

    return (
        <div className="flex flex-col md:flex-row min-h-[80vh] h-[calc(100vh-12rem)] gap-6 p-4 md:p-6 text-white max-w-7xl mx-auto w-full">

            {/* Sidebar: Lists Collection */}
            <div className={`w-full md:w-1/3 glass-card rounded-2xl p-4 overflow-y-auto flex flex-col bg-white/5 border border-white/10 ${activeList ? "hidden md:flex" : "flex"}`}>
                <h2 className="text-xl font-bold mb-4">Your Lists</h2>

                <form onSubmit={handleCreateList} className="space-y-2 mb-4">
                    <input
                        value={newListName}
                        onChange={(e) => setNewListName(e.target.value)}
                        placeholder="List name..."
                        className="w-full p-2 text-sm rounded-lg bg-white/5 border border-white/10 target:outline-none"
                        required
                    />
                    <input
                        value={newListDesc}
                        onChange={(e) => setNewListDesc(e.target.value)}
                        placeholder="Description (optional)..."
                        className="w-full p-2 text-xs rounded-lg bg-white/5 border border-white/10 target:outline-none"
                    />
                    <button type="submit" className="w-full bg-brand text-xs font-bold py-2 rounded-lg hover:bg-brand/90 transition-colors">
                        Create List
                    </button>
                </form>

                <div className="flex-1 space-y-2">
                    {lists.map((list) => (
                        <div
                            key={list.id}
                            className={`p-3 rounded-xl border cursor-pointer flex justify-between items-center ${
                                activeList?.id === list.id ? "bg-brand/20 border-brand/30" : "bg-white/5 border-transparent"
                            }`}
                            onClick={() => setActiveList(list)}
                        >
                            <div className="min-w-0 flex-1">
                                <p className="font-semibold text-sm truncate">{list.name}</p>
                                {list.description && <p className="text-xs opacity-60 truncate">{list.description}</p>}
                            </div>
                            <button
                                onClick={(e) => { e.stopPropagation(); handleDeleteList(list.id); }}
                                className="text-red-400 hover:scale-110 ml-2 p-1"
                            >
                                ✕
                            </button>
                        </div>
                    ))}
                </div>
            </div>

            {/* Main Panel: View List Content */}
            <div className={`w-full md:flex-1 glass-card rounded-2xl p-5 overflow-hidden flex flex-col bg-white/5 border border-white/10 ${!activeList ? "hidden md:flex" : "flex"}`}>
                {activeList && (
                    <button
                        onClick={() => setActiveList(null)}
                        className="md:hidden text-brand text-xs font-semibold mb-3 self-start"
                    >
                        ← Back to lists
                    </button>
                )}

                <div className="flex justify-between items-start pb-3 border-b border-white/10 mb-4">
                    <div>
                        <h2 className="text-xl font-bold">{activeList?.name ?? "Select a list"}</h2>
                        {activeList?.description && <p className="text-xs text-gray-400">{activeList.description}</p>}
                    </div>
                    {activeList && (
                        <button
                            type="button"
                            className="bg-white/10 hover:bg-white/15 border border-white/10 text-xs font-semibold px-3 py-1.5 rounded-xl transition-all"
                            onClick={() => {
                                setSearchOpen((v) => !v);
                                if (searchOpen) {
                                    setHasSearched(false);
                                    setSearchResults([]);
                                }
                            }}
                        >
                            {searchOpen ? "− Close Search" : "+ Add Item"}
                        </button>
                    )}
                </div>

                {!activeList ? (
                    <p className="opacity-60 text-sm m-auto text-center">Select a list from the sidebar to view its items.</p>
                ) : (
                    <div className="flex-1 overflow-y-auto space-y-4 pr-1 custom-scrollbar">

                        {/* Inline Search Panel Dropdown */}
                        <AnimatePresence>
                            {searchOpen && (
                                <motion.div
                                    initial={{ opacity: 0, height: 0 }}
                                    animate={{ opacity: 1, height: "auto" }}
                                    exit={{ opacity: 0, height: 0 }}
                                    className="border-b border-white/10 pb-4 overflow-hidden"
                                >
                                    <div className="grid grid-cols-3 gap-2 mb-3">
                                        {QUERY_TYPES.map(({ value, label, icon }) => (
                                            <button
                                                key={value}
                                                type="button"
                                                className={`flex items-center justify-center gap-1.5 p-2 rounded-xl border text-xs font-medium transition-all ${
                                                    queryType === value
                                                        ? "bg-brand/20 border-brand text-white"
                                                        : "bg-white/5 border-white/5 text-white/60 hover:bg-white/10"
                                                }`}
                                                onClick={() => handleQueryTypeChange(value)}
                                            >
                                                <span>{icon}</span>
                                                <span>{label}</span>
                                            </button>
                                        ))}
                                    </div>

                                    <SearchBar
                                        queryType={queryType}
                                        onSearchAction={handleSearch}
                                        showBackButton={false}
                                    />

                                    {(searchLoading || hasSearched) && (
                                        <div className="mt-2 max-w-full max-h-[30vh] overflow-y-auto bg-black/20 rounded-xl p-2 border border-white/5">
                                            <ResultsList
                                                results={searchResults}
                                                loading={searchLoading}
                                                error={searchError}
                                                mode="select"
                                                onAddAction={handleAddItemFromSearch}
                                                showBackButton={false}
                                            />
                                        </div>
                                    )}
                                </motion.div>
                            )}
                        </AnimatePresence>

                        {/* List Stack Items */}
                        <div className="space-y-2">
                            {items.map((item, index) => {
                                const title = "title" in item ? item.title : item.name;
                                const id = item.resultType === "exercise" ? item.exerciseId : item.id;

                                // Dynamic extraction matching your SearchResult structural paradigms
                                const mediaSrc =
                                    item.resultType === "book"
                                        ? (item as any).thumbnailUrl
                                        : item.resultType === "movie"
                                            ? (item as any).poster
                                            : (item as any).gifUrl;

                                return (
                                    <div
                                        key={`${id}-${index}`}
                                        onClick={() => setSelectedModalItem(item)}
                                        className="group flex justify-between items-center p-3 bg-white/5 hover:bg-white/10 hover:border-white/20 rounded-xl border border-white/5 gap-4 cursor-pointer transition-all duration-200"
                                    >
                                        <div className="flex items-center gap-3 min-w-0 flex-1">

                                            {/* Reordering Controls (stopPropagation preserves operational purity) */}
                                            <div
                                                className="flex flex-col gap-0.5 shrink-0"
                                                onClick={(e) => e.stopPropagation()}
                                            >
                                                <button
                                                    disabled={index === 0}
                                                    onClick={() => moveItem(index, "up")}
                                                    className="text-[10px] opacity-40 hover:opacity-100 disabled:opacity-10 py-0.5 px-1 bg-white/5 rounded hover:bg-white/10"
                                                >
                                                    ▲
                                                </button>
                                                <button
                                                    disabled={index === items.length - 1}
                                                    onClick={() => moveItem(index, "down")}
                                                    className="text-[10px] opacity-40 hover:opacity-100 disabled:opacity-10 py-0.5 px-1 bg-white/5 rounded hover:bg-white/10"
                                                >
                                                    ▼
                                                </button>
                                            </div>

                                            {/* Visual Row Thumbnail */}
                                            {mediaSrc ? (
                                                <div className="w-12 h-16 relative shrink-0 rounded-lg overflow-hidden border border-white/10 bg-black/20">
                                                    <img
                                                        src={mediaSrc}
                                                        alt={title}
                                                        className="w-full h-full object-cover"
                                                        loading="lazy"
                                                    />
                                                </div>
                                            ) : (
                                                /* Fallback geometric icon badge based on type when no media properties are present */
                                                <div className="w-12 h-16 shrink-0 rounded-lg bg-white/5 border border-white/10 flex items-center justify-center text-lg">
                                                    {item.resultType === "book" ? "📚" : item.resultType === "movie" ? "🎬" : "🏋️"}
                                                </div>
                                            )}

                                            {/* Text Parameters Info */}
                                            <div className="min-w-0 flex-1">
                                                <p className="font-semibold text-sm truncate text-white group-hover:text-brand transition-colors">
                                                    {title}
                                                </p>
                                                <span className="inline-block text-[9px] tracking-wide bg-white/10 px-1.5 py-0.5 rounded text-gray-300 font-bold uppercase mt-0.5">
                                                    {item.resultType}
                                                </span>
                                                {item.notes && item.notes !== title && (
                                                    <p className="text-xs text-gray-400 truncate mt-1 italic opacity-80">
                                                        Note: {item.notes}
                                                    </p>
                                                )}
                                            </div>
                                        </div>

                                        {/* Remove Action (stopPropagation protects against triggering details modal) */}
                                        <button
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                handleRemoveItem(item);
                                            }}
                                            className="text-xs text-gray-400 hover:text-red-400 bg-white/5 hover:bg-white/10 px-3 py-2 rounded-xl border border-white/10 shrink-0 transition-colors relative z-10"
                                        >
                                            Remove
                                        </button>
                                    </div>
                                );
                            })}

                            {items.length === 0 && !searchOpen && (
                                <p className="text-sm text-gray-400 text-center py-12">This list is currently empty.</p>
                            )}
                        </div>
                    </div>
                )}
            </div>

            {/* Global Context Portal mount point for active details tracking */}
            {selectedModalItem && (
                <ResultDetailModal
                    result={selectedModalItem}
                    onClose={() => setSelectedModalItem(null)}
                />
            )}

        </div>
    );
}