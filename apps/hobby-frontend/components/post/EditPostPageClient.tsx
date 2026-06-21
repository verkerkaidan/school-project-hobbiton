"use client";

import { useRef, useState } from "react";
import { useRouter } from "next/navigation";
import { motion, AnimatePresence } from "framer-motion";
import { GymExercise } from "@/components/createPost/GymExercise";
import { ReadingFields } from "@/components/createPost/ReadingFields";
import SearchBar from "@/components/discover/SearchBar";
import ResultsList, { ResultItem } from "@/components/discover/ResultsList";
import { updatePost } from "@/lib/postClient";
import { LinkedItem } from "@/types/LinkedItem";
import { Post, Gym, Reading, LinkedHobbyItem, Exercise } from "@/types/posts";
import HobbyType from "@/types/enums";
import Cookies from "js-cookie";
import { useQueryClient } from "@tanstack/react-query";
import { FEED_QUERY_KEY } from "@/components/feed/Feed";

type QueryType = "exercise" | "book" | "movie";

const QUERY_TYPES: { value: QueryType; label: string; icon: string }[] = [
  { value: "exercise", label: "Exercise", icon: "🏋️" },
  { value: "book", label: "Book", icon: "📚" },
  { value: "movie", label: "Movie", icon: "🎬" },
];

const HOBBY_LABELS: Record<string, { label: string; icon: string }> = {
  GYM: { label: "Gym", icon: "🏋️" },
  READING: { label: "Reading", icon: "📚" },
  OTHER: { label: "Other", icon: "✨" },
};

function linkedHobbyToLinkedItem(item: LinkedHobbyItem): LinkedItem {
  return {
    type: item.hobbyType as LinkedItem["type"],
    title: item.displayName,
    imageUrl: item.imageUrl,
    externalId: item.externalId,
  };
}

interface Props {
  post: Post;
}

export default function EditPostPageClient({ post }: Props) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const hobbyType = post.hobbyType?.toUpperCase() as string;
  const gymPost = hobbyType === "GYM" ? (post as Gym) : undefined;
  const readingPost = hobbyType === "READING" ? (post as Reading) : undefined;

  const [title, setTitle] = useState(post.title);
  const [description, setDescription] = useState(post.description);
  const [exercises, setExercises] = useState<Exercise[]>(
    gymPost?.exercises ? [...gymPost.exercises] : []
  );
  const [readingForm, setReadingForm] = useState<{ bookTitle: string; pagesRead: number; totalPagesRead: number; bookFinished: boolean }>({
    bookTitle: readingPost?.bookTitle ?? "",
    pagesRead: readingPost?.pagesRead ?? 0,
    totalPagesRead: readingPost?.totalPagesRead ?? 0,
    bookFinished: readingPost?.bookFinished ?? false,
  });

  const [linkedItems, setLinkedItems] = useState<LinkedItem[]>(
    (post.linkedItems ?? []).map(linkedHobbyToLinkedItem)
  );
  const [queryType, setQueryType] = useState<QueryType | null>(null);
  const [searchResults, setSearchResults] = useState<ResultItem[]>([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState("");
  const [searchOpen, setSearchOpen] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);

  // Existing media: track which ones the user wants to keep
  const [keptMediaIds, setKeptMediaIds] = useState<string[]>(
    (post.mediaSet ?? []).map((m) => m.relevantId)
  );
  // New files to upload
  const [newFiles, setNewFiles] = useState<File[]>([]);
  const [newPreviews, setNewPreviews] = useState<string[]>([]);
  const [dragOver, setDragOver] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const totalPhotos = keptMediaIds.length + newFiles.length;

  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const hobbyLabel = HOBBY_LABELS[hobbyType] ?? { label: hobbyType, icon: "✨" };

  const handleGymChange = (index: number, field: keyof Exercise, value: string) => {
    setExercises((prev) =>
      prev.map((ex, i) =>
        i === index ? { ...ex, [field]: field === "title" ? value : Number(value) } : ex
      )
    );
  };

  const addExercise = () =>
    setExercises((prev) => [...prev, { title: "", reps: 0, weight: 0, rpe: 0 }]);

  const removeExercise = (index: number) =>
    setExercises((prev) => prev.filter((_, i) => i !== index));

  const handleReadingChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    let parsed: string | number | boolean = value;
    if (type === "checkbox") parsed = checked;
    else if (type === "number") parsed = Number(value);
    setReadingForm((prev) => ({ ...prev, [name]: parsed }));
  };

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
      setSearchError(e instanceof Error ? e.message : "Search error");
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

  const addLinkedItem = (item: ResultItem) => {
    let linked: LinkedItem;
    if (item.resultType === "book") {
      linked = { title: item.title, imageUrl: item.thumbnailUrl || "", externalId: item.id || "", type: "BOOK" };
    } else if (item.resultType === "exercise") {
      linked = { title: item.name, imageUrl: item.gifUrl || "", externalId: item.exerciseId, type: "EXERCISE" };
    } else if (item.resultType === "movie") {
      linked = { title: item.title, imageUrl: item.poster || "", externalId: item.id, type: "MOVIE" };
    } else {
      return;
    }
    setLinkedItems((prev) => {
      if (prev.some((li) => li.externalId === linked.externalId)) return prev;
      return [...prev, linked];
    });
  };

  const removeLinkedItem = (externalId: string | undefined) =>
    setLinkedItems((prev) => prev.filter((li) => li.externalId !== externalId));

  const addNewFiles = (files: FileList | File[]) => {
    const images = Array.from(files).filter((f) => f.type.startsWith("image/"));
    const slots = 3 - totalPhotos;
    if (slots <= 0) { setError("You can upload up to 3 images total."); return; }
    const toAdd = images.slice(0, slots);
    setNewFiles((prev) => [...prev, ...toAdd]);
    setNewPreviews((prev) => [...prev, ...toAdd.map((f) => URL.createObjectURL(f))]);
  };

  const removeNewFile = (index: number) => {
    URL.revokeObjectURL(newPreviews[index]);
    setNewFiles((prev) => prev.filter((_, i) => i !== index));
    setNewPreviews((prev) => prev.filter((_, i) => i !== index));
  };

  const removeExistingMedia = (relevantId: string) =>
    setKeptMediaIds((prev) => prev.filter((id) => id !== relevantId));

  const validate = (): boolean => {
    if (!title.trim()) { setError("Title is required."); return false; }
    if (!description.trim()) { setError("Description is required."); return false; }
    if (hobbyType === "GYM") {
      for (const [i, ex] of exercises.entries()) {
        if (!ex.title.trim()) { setError(`Exercise ${i + 1}: title is required.`); return false; }
        if (ex.reps <= 0) { setError(`Exercise ${i + 1}: reps must be greater than 0.`); return false; }
        if (ex.weight <= 0) { setError(`Exercise ${i + 1}: weight must be greater than 0.`); return false; }
        if (ex.rpe < 1 || ex.rpe > 10) { setError(`Exercise ${i + 1}: RPE must be between 1 and 10.`); return false; }
      }
    }
    if (hobbyType === "READING") {
      if (!readingForm.bookTitle.trim()) { setError("Book title is required."); return false; }
      if (readingForm.pagesRead <= 0) { setError("Pages read must be greater than 0."); return false; }
      if (readingForm.totalPagesRead <= 0) { setError("Total pages must be greater than 0."); return false; }
      if (readingForm.pagesRead > readingForm.totalPagesRead) { setError("Pages read cannot exceed total pages."); return false; }
    }
    return true;
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!validate()) return;

    const userId = Cookies.get("userId")!;
    const existingMediaUuids = keptMediaIds;

    const mappedLinkedItems = linkedItems.map((li) => ({
      displayName: li.title,
      hobbyType: li.type,
      imageUrl: li.imageUrl ?? "",
      externalId: li.externalId ?? "",
    }));

    const payload: Record<string, unknown> = {
      type: hobbyType,
      title,
      description,
      userId,
      linkedItems: mappedLinkedItems,
      mediaInstanceSet: existingMediaUuids,
    };

    if (hobbyType === "GYM") {
      payload.exercises = exercises;
    } else if (hobbyType === "READING") {
      payload.bookTitle = readingForm.bookTitle;
      payload.pagesRead = readingForm.pagesRead;
      payload.totalPagesRead = readingForm.totalPagesRead;
      payload.bookFinished = readingForm.bookFinished;
    }

    try {
      setLoading(true);
      await updatePost(post.id, payload, newFiles);
      queryClient.invalidateQueries({ queryKey: [FEED_QUERY_KEY] });
      router.push(`/posts/${post.id}`);
      router.refresh();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Update failed.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="max-w-5xl mx-auto px-4 py-8 md:py-12 np-root">
      {/* Page header */}
      <div className="mb-8">
        <button
          type="button"
          onClick={() => router.back()}
          className="inline-flex items-center gap-1.5 text-xs text-accent/50 hover:text-accent/80 transition-colors mb-4"
        >
          ← Back
        </button>
        <h1 className="text-2xl md:text-3xl font-extrabold tracking-tight text-accent">Edit Post</h1>
        <p className="text-sm text-accent/55 mt-1">Update your post details below.</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="glass-card rounded-2xl p-6 md:p-8 grid grid-cols-1 lg:grid-cols-12 gap-6 lg:gap-8">

        {/* LEFT: core fields */}
        <div className="lg:col-span-6 flex flex-col gap-4 md:gap-5">

          {/* Post type (read-only) */}
          <div className="flex items-center gap-2 px-4 py-3 bg-white/[0.03] border border-white/10 rounded-xl text-sm text-accent/70">
            <span>{hobbyLabel.icon}</span>
            <span>{hobbyLabel.label}</span>
            <span className="ml-auto text-xs text-accent/50">cannot be changed</span>
          </div>

          <div className="np-field">
            <label className="np-label">Title</label>
            <input
              name="title"
              type="text"
              placeholder="Post title"
              value={title}
              onChange={(e) => { setTitle(e.target.value); setError(""); }}
              className="bg-brand/5 np-input"
            />
          </div>

          <div className="np-field">
            <label className="np-label">Description</label>
            <textarea
              name="description"
              placeholder="How did it go?"
              value={description}
              onChange={(e) => { setDescription(e.target.value); setError(""); }}
              rows={4}
              className="np-textarea bg-brand/5"
            />
          </div>

          {hobbyType === HobbyType.GYM && (
            <div className="np-field">
              <label className="np-label">Exercises</label>
              <div className="np-exercises">
                {exercises.map((ex, i) => (
                  <GymExercise key={i} ex={ex} index={i} onRemove={removeExercise} onChange={handleGymChange} />
                ))}
                <button type="button" onClick={addExercise} className="np-add-exercise-btn w-full mt-1">
                  + Add exercise
                </button>
              </div>
            </div>
          )}

          {hobbyType === HobbyType.READING && (
            <ReadingFields
              form={{
                ...readingForm,
                title,
                description,
                totalDuration: 0,
                userId: "",
                mediaInstanceDTOSet: [],
                type: HobbyType.READING,
              }}
              onChange={handleReadingChange}
            />
          )}
        </div>

        {/* RIGHT: linked items */}
        <div className="lg:col-span-6 flex flex-col gap-5 lg:border-l lg:border-white/8 lg:pl-8 border-t border-white/8 lg:border-t-0 pt-6 lg:pt-0">
          <div className="np-section h-full flex flex-col">
            <div>
              <div className="np-section-header mb-3">
                <label className="np-label">
                  Linked items
                  {linkedItems.length > 0 && <span className="np-badge ml-1">{linkedItems.length}</span>}
                </label>
                <button
                  type="button"
                  className="np-toggle-btn text-xs md:text-sm px-3 py-1.5"
                  onClick={() => {
                    setSearchOpen((v) => !v);
                    if (searchOpen) {
                      setHasSearched(false);
                      setSearchResults([]);
                    }
                  }}
                >
                  {searchOpen ? "− Close" : "+ Add item"}
                </button>
              </div>

              {linkedItems.length > 0 && (
                <div className="np-chips mb-4">
                  {linkedItems.map((li) => (
                    <div key={li.externalId} className="np-chip flex-wrap sm:flex-nowrap gap-2 p-3">
                      {li.imageUrl ? (
                        <img src={li.imageUrl} alt={li.title} className="np-chip-img w-10 h-10 md:w-12 md:h-12" />
                      ) : (
                        <div className="np-chip-img np-chip-placeholder w-10 h-10 md:w-12 md:h-12" />
                      )}
                      <span className="np-chip-title text-xs md:text-sm min-w-[120px] truncate flex-1">{li.title}</span>
                      <span className="np-chip-type text-[10px] md:text-xs">{li.type}</span>
                      <button
                        type="button"
                        className="np-chip-remove text-lg ml-auto"
                        onClick={() => removeLinkedItem(li.externalId)}
                      >
                        ×
                      </button>
                    </div>
                  ))}
                </div>
              )}

              <AnimatePresence>
                {searchOpen && (
                  <motion.div
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: "auto" }}
                    exit={{ opacity: 0, height: 0 }}
                    className="np-search-panel border-t border-white/5 pt-3 overflow-hidden"
                    onSubmit={(e) => e.stopPropagation()}
                  >
                    <div className="grid grid-cols-3 gap-1.5 md:gap-2 mb-3">
                      {QUERY_TYPES.map(({ value, label, icon }) => (
                        <button
                          key={value}
                          type="button"
                          className={`np-type-btn flex-col md:flex-row p-2 text-xs md:text-sm gap-1 ${queryType === value ? "np-type-btn--active" : ""}`}
                          onClick={() => handleQueryTypeChange(value)}
                        >
                          <span className="np-type-icon text-sm md:text-base">{icon}</span>
                          <label className="text-[11px] md:text-xs font-medium">{label}</label>
                        </button>
                      ))}
                    </div>

                    {queryType && (
                      <div className="w-full">
                        <SearchBar queryType={queryType} onSearchAction={handleSearch} showBackButton={false} />
                        {(searchLoading || hasSearched) && (
                          <div className="np-results-wrap mt-2 max-w-full">
                            <ResultsList
                              results={searchResults}
                              loading={searchLoading}
                              error={searchError}
                              mode="select"
                              onAddAction={addLinkedItem}
                              showBackButton={false}
                            />
                          </div>
                        )}
                      </div>
                    )}
                  </motion.div>
                )}
              </AnimatePresence>
            </div>

            {/* Photos */}
            <div className="np-field mt-6">
              <div className="flex items-center justify-between mb-1.5">
                <label className="np-label">Photos</label>
                <span className="text-xs opacity-60">{totalPhotos}/3</span>
              </div>

              {/* Existing photos */}
              {(post.mediaSet ?? []).length > 0 && (
                <div className="flex gap-2 mb-3 flex-wrap">
                  {post.mediaSet.map((m) => {
                    const kept = keptMediaIds.includes(m.relevantId);
                    return (
                      <div key={m.relevantId} className="relative flex-shrink-0">
                        <img
                          src={`data:${m.contentType};base64,${m.fileData}`}
                          alt="existing photo"
                          className={`w-16 h-16 rounded-xl object-cover border border-white/10 transition-opacity ${kept ? "opacity-100" : "opacity-30"}`}
                        />
                        <button
                          type="button"
                          onClick={() =>
                            kept
                              ? removeExistingMedia(m.relevantId)
                              : setKeptMediaIds((prev) => [...prev, m.relevantId])
                          }
                          className="absolute -top-1.5 -right-1.5 w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold transition-colors bg-black/60 border border-white/20 text-accent hover:bg-rose-500/80"
                        >
                          {kept ? "×" : "+"}
                        </button>
                      </div>
                    );
                  })}
                </div>
              )}

              {/* Upload new photos */}
              {totalPhotos < 3 && (
                <button
                  type="button"
                  onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
                  onDragLeave={() => setDragOver(false)}
                  onDrop={(e) => { e.preventDefault(); setDragOver(false); if (e.dataTransfer.files) addNewFiles(e.dataTransfer.files); }}
                  onClick={() => fileInputRef.current?.click()}
                  className={`np-dropzone p-4 md:p-6 ${dragOver ? "np-dropzone--active" : ""}`}
                >
                  <label className="np-dropzone-text text-xs md:text-sm">Click or drag & drop to add photos</label>
                </button>
              )}
              <input
                ref={fileInputRef}
                type="file"
                accept="image/jpeg,image/webp,image/png"
                multiple
                className="hidden"
                onChange={(e) => { if (e.target.files) addNewFiles(e.target.files); if (fileInputRef.current) fileInputRef.current.value = ""; }}
              />

              {/* New file previews */}
              {newPreviews.length > 0 && (
                <div className="np-previews mt-2 flex gap-2 overflow-x-auto py-1">
                  {newPreviews.map((src, i) => (
                    <div key={i} className="np-preview-wrap flex-shrink-0">
                      <img src={src} alt="new upload preview" className="np-preview-img w-14 h-14 md:w-16 md:h-16" />
                      <button type="button" onClick={() => removeNewFile(i)} className="np-preview-remove">×</button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>

        </div>{/* close glass-card grid */}

        {/* ACTIONS */}
        <div className="flex flex-col-reverse sm:flex-row items-center justify-end gap-3 pt-2">
          {error && (
            <p className="np-error text-xs md:text-sm mr-auto w-full sm:w-auto mb-2 sm:mb-0">{error}</p>
          )}
          <div className="flex w-full sm:w-auto items-center justify-end gap-3">
            <button
              type="button"
              onClick={() => router.back()}
              className="px-4 py-2 text-sm font-medium text-accent/65 hover:text-accent border border-white/15 hover:border-white/30 rounded-xl transition-all bg-transparent"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="np-submit-btn w-full sm:w-auto min-w-[120px] px-4 py-2 text-sm"
            >
              {loading ? "Saving…" : "Save Changes"}
            </button>
          </div>
        </div>
      </form>
    </main>
  );
}
