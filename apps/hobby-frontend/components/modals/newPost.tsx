"use client";

import { useRef, useState } from "react";
import { Modal } from "./Modal";
import { useRouter } from "next/navigation";
import { GymExercise } from "../createPost/GymExercise";
import { ReadingFields } from "../createPost/ReadingFields";
import { postCreateRequest } from "@/lib/graphQlClient";
import { LinkedItem } from "@/types/LinkedItem";
import SearchBar from "@/components/discover/SearchBar";
import ResultsList, { ResultItem } from "@/components/discover/ResultsList";
import { BaseCreatePost, CreateGymPost, CreatePost, CreateReadingPost, ModalProps } from "@/types/interfaces";
import Cookies from "js-cookie";
import HobbyType from "@/types/enums";
import { motion, AnimatePresence } from "framer-motion";

type QueryType = "exercise" | "book" | "movie";

const QUERY_TYPES: { value: QueryType; label: string; icon: string }[] = [
  { value: "exercise", label: "Exercise", icon: "🏋️" },
  { value: "book",     label: "Book",     icon: "📚" },
  { value: "movie",    label: "Movie",    icon: "🎬" },
];

const HOBBY_OPTIONS = [
  { value: HobbyType.GYM, label: "Gym", icon: "🏋️" },
  { value: HobbyType.READING, label: "Reading", icon: "📚" },
  { value: HobbyType.OTHER, label: "Other", icon: "✨" },
];

interface Exercise {
  title: string;
  reps: number;
  weight: number;
  rpe: number;
}

const BASE_DEFAULTS: BaseCreatePost = {
  description: "",
  linkedItems: [],
  mediaInstanceDTOSet: [],
  title: "",
  totalDuration: 0,
  type: HobbyType.OTHER,
  userId: ""
};

const GYM_DEFAULTS: CreateGymPost = {
  ...BASE_DEFAULTS,
  type: HobbyType.GYM,
  exercises: [],
};

const READING_DEFAULTS: CreateReadingPost = {
  ...BASE_DEFAULTS,
  type: HobbyType.READING,
  bookTitle: "",
  pagesRead: 0,
  totalPagesRead: 0,
  bookFinished: false,
};

const GENERIC_DEFAULTS = {
  ...BASE_DEFAULTS,
  type: HobbyType.OTHER,
};

export function CreatePostModal({ isOpen, onClose }: ModalProps) {
  const [form, setForm] = useState<CreatePost>(GYM_DEFAULTS);
  const [error, setError] = useState<string>("");
  const [loading, setLoading] = useState(false);
  const [imagePreviews, setImagePreviews] = useState<string[]>([]);
  const [dragOver, setDragOver] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const router = useRouter();

  // ── Linked items ─────────────────────────────────────────────────────────────
  const [linkedItems, setLinkedItems] = useState<LinkedItem[]>([]);
  const [queryType, setQueryType] = useState<QueryType | null>(null);
  const [searchResults, setSearchResults] = useState<ResultItem[]>([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState("");
  const [searchOpen, setSearchOpen] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);

  const handlePostTypeChange = (newType: HobbyType) => {
    if (newType === HobbyType.GYM) {
      setForm({
        ...GYM_DEFAULTS,
        title: form.title,
        description: form.description,
        totalDuration: form.totalDuration,
      });
    } else if (newType === HobbyType.READING) {
      setForm({
        ...READING_DEFAULTS,
        title: form.title,
        description: form.description,
        totalDuration: form.totalDuration,
      });
    } else {
      setForm({
        ...GENERIC_DEFAULTS,
        title: form.title,
        description: form.description,
        totalDuration: 0,
      } as any);
    }
    setError("");
  };

  const handleBaseChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    setForm((prev) => ({...prev, [e.target.name]: e.target.value}));
    setError("");
  };

  const handleGymChange = (index: number, field: keyof Exercise, value: string) => {
    if (form.type !== HobbyType.GYM) return;
    const updated = form.exercises.map((ex, i) =>
        i === index ? {...ex, [field]: field === "title" ? value : Number(value)} : ex
    );
    setForm({...form, exercises: updated});
    setError("");
  };

  const addExercise = () => {
    if (form.type !== HobbyType.GYM) return;
    setForm({...form, exercises: [...form.exercises, {title: "", reps: 0, weight: 0, rpe: 0}]});
  };

  const removeExercise = (index: number) => {
    if (form.type !== HobbyType.GYM) return;
    setForm({...form, exercises: form.exercises.filter((_, i) => i !== index)});
  };

  const handleReadingChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (form.type !== HobbyType.READING) return;
    const {name, value, type, checked} = e.target;
    let parsed: string | number | boolean = value;
    if (type === "checkbox") parsed = checked;
    else if (type === "number") parsed = Number(value);
    setForm({...form, [name]: parsed});
    setError("");
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
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({type: queryType.toUpperCase(), query, maxResults: 10}),
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

  const addLinkedItem = (item: ResultItem) => {
    let linkedItem: LinkedItem;
    if (item.resultType === "book") {
      linkedItem = {title: item.title, imageUrl: item.thumbnailUrl || "", externalId: item.id || "", type: "BOOK"};
    } else if (item.resultType === "exercise") {
      linkedItem = {title: item.name, imageUrl: item.gifUrl || "", externalId: item.exerciseId, type: "EXERCISE"};
    } else {
      linkedItem = {title: item.title, imageUrl: item.poster || "", externalId: item.id, type: "MOVIE"};
    }

    setLinkedItems((prev) => {
      if (prev.some((li) => li.externalId === linkedItem.externalId)) return prev;
      return [...prev, linkedItem];
    });
  };

  const removeLinkedItem = (externalId: string | undefined) => {
    setLinkedItems((prev) => prev.filter((li) => li.externalId !== externalId));
  };

  const validate = (): boolean => {
    if (!form.title.trim()) {
      setError("Title is required.");
      return false;
    }
    if (!form.description.trim()) {
      setError("Description is required.");
      return false;
    }
    if (form.type !== HobbyType.OTHER && (!form.totalDuration || form.totalDuration <= 0)) {
      setError("Total duration must be greater than 0.");
      return false;
    }
    if (form.type === HobbyType.GYM) {
      if (form.exercises.length === 0) {
        setError("Add at least one exercise.");
        return false;
      }
      for (const [i, ex] of form.exercises.entries()) {
        if (!ex.title.trim()) {
          setError(`Exercise ${i + 1}: title is required.`);
          return false;
        }
        if (ex.reps <= 0) {
          setError(`Exercise ${i + 1}: reps must be greater than 0.`);
          return false;
        }
        if (ex.weight <= 0) {
          setError(`Exercise ${i + 1}: weight must be greater than 0.`);
          return false;
        }
        if (ex.rpe < 1 || ex.rpe > 10) {
          setError(`Exercise ${i + 1}: RPE must be between 1 and 10.`);
          return false;
        }
      }
    }
    if (form.type === HobbyType.READING) {
      if (!form.bookTitle.trim()) {
        setError("Book title is required.");
        return false;
      }
      if (form.pagesRead <= 0) {
        setError("Pages read must be greater than 0.");
        return false;
      }
      if (form.totalPagesRead <= 0) {
        setError("Total pages must be greater than 0.");
        return false;
      }
      if (form.pagesRead > form.totalPagesRead) {
        setError("Pages read cannot exceed total pages.");
        return false;
      }
    }
    return true;
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!validate()) return;
    form.userId = Cookies.get("userId")!;
    try {
      setLoading(true);
      await postCreateRequest({...form, linkedItems});
      router.push("/profile");
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Creation of post failed.");
    } finally {
      setLoading(false);
    }
  };

  const addImages = (files: FileList | File[]) => {
    const arr = Array.from(files).filter((f) => f.type.startsWith("image/"));
    const remaining = 3 - form.mediaInstanceDTOSet.length;
    if (remaining <= 0) {
      setError("You can upload up to 3 images.");
      return;
    }
    const toAdd = arr.slice(0, remaining);
    const newPreviews = toAdd.map((f) => URL.createObjectURL(f));
    setForm((prev) => ({...prev, mediaInstanceDTOSet: [...prev.mediaInstanceDTOSet, ...toAdd]}));
    setImagePreviews((prev) => [...prev, ...newPreviews]);
  };

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) addImages(e.target.files);
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const removeImage = (index: number) => {
    URL.revokeObjectURL(imagePreviews[index]);
    setForm((prev) => ({...prev, mediaInstanceDTOSet: prev.mediaInstanceDTOSet.filter((_, i) => i !== index)}));
    setImagePreviews((prev) => prev.filter((_, i) => i !== index));
  };

  const currentHobby = HOBBY_OPTIONS.find(opt => opt.value === form.type) || HOBBY_OPTIONS[2];

  return (
      <Modal isOpen={isOpen} onClose={onClose} largeModal={true}>
        <div className="np-root w-full mx-auto">
          <div className="np-header mb-6">
            <h2 className="np-title text-xl md:text-2xl">New Post</h2>
            <p className="np-subtitle text-xs md:text-sm">Share what you&#39;ve been up to</p>
          </div>

          <form onSubmit={handleSubmit} className="grid grid-cols-1 lg:grid-cols-12 gap-6 lg:gap-8">

            {/* LEFT SIDE: Title, Description, Type, Specific Fields */}
            <div className="lg:col-span-6 flex flex-col gap-4 md:gap-5">

              {/* Custom Dynamic Dropdown */}
              <div className="relative z-30">
                <label className="np-label mb-1.5 block">Post Type</label>
                <button
                    type="button"
                    onClick={() => setDropdownOpen(!dropdownOpen)}
                    className="w-full flex items-center justify-between bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-sm text-white focus:border-[#7c6af5] transition-colors"
                >
              <span className="flex items-center gap-2">
                <span>{currentHobby.icon}</span>
                <span>{currentHobby.label}</span>
              </span>
                  <motion.span animate={{rotate: dropdownOpen ? 180 : 0}} className="text-xs opacity-60">
                    ▼
                  </motion.span>
                </button>

                <AnimatePresence>
                  {dropdownOpen && (
                      <motion.div
                          initial={{opacity: 0, y: -8}}
                          animate={{opacity: 1, y: 0}}
                          exit={{opacity: 0, y: -8}}
                          transition={{duration: 0.15}}
                          className="absolute left-0 right-0 mt-2 bg-[var(--color-background-secondary)] border border-white/10 rounded-xl shadow-xl overflow-hidden z-50">
                        {HOBBY_OPTIONS.map((opt) => (
                            <button
                                key={opt.value}
                                type="button"
                                onClick={() => {
                                  handlePostTypeChange(opt.value);
                                  setDropdownOpen(false);
                                }}
                                className="w-full flex items-center gap-2 px-4 py-3 text-sm text-white/80 hover:bg-white/5 hover:text-white text-left transition-colors"
                            >
                              <span>{opt.icon}</span>
                              <span>{opt.label}</span>
                            </button>
                        ))}
                      </motion.div>
                  )}
                </AnimatePresence>
              </div>

              <div className="np-field">
                <label className="np-label">Title</label>
                <input name="title" type="text" placeholder="Post title" value={form.title} onChange={handleBaseChange}
                       className="np-input"/>
              </div>

              <div className="np-field">
                <label className="np-label">Description</label>
                <textarea name="description" placeholder="How did it go?" value={form.description}
                          onChange={handleBaseChange} rows={4} className="np-textarea"/>
              </div>

              {form.type !== HobbyType.OTHER && (
                  <div className="np-field">
                    <label className="np-label">Total duration (minutes)</label>
                    <input name="totalDuration" type="number" min={0} step={0.25} placeholder="55"
                           value={form.totalDuration || ""} onChange={handleBaseChange} className="np-input"/>
                  </div>
              )}

              {form.type === HobbyType.GYM && (
                  <div className="np-field">
                    <label className="np-label">Exercises</label>
                    <div className="np-exercises">
                      {form.exercises.map((ex, i) => (
                          <GymExercise key={i} ex={ex} index={i} onRemove={removeExercise} onChange={handleGymChange}/>
                      ))}
                      <button type="button" onClick={addExercise} className="np-add-exercise-btn w-full mt-1">
                        + Add exercise
                      </button>
                    </div>
                  </div>
              )}

              {form.type === HobbyType.READING && (
                  <ReadingFields form={form as CreateReadingPost} onChange={handleReadingChange}/>
              )}
            </div>

            {/* RIGHT SIDE: Linked items & Media uploads */}
            <div
                className="lg:col-span-6 flex flex-col gap-5 lg:border-l lg:border-white/10 lg:pl-8 border-t border-white/5 lg:border-t-0 pt-6 lg:pt-0">
              <div className="np-section h-full flex flex-col justify-between">
                <div>
                  <div className="np-section-header mb-3">
                <span className="np-label">
                  Linked items
                  {linkedItems.length > 0 && <span className="np-badge ml-1">{linkedItems.length}</span>}
                </span>
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
                              {li.imageUrl ? <img src={li.imageUrl} alt={li.title}
                                                  className="np-chip-img w-10 h-10 md:w-12 md:h-12"/> :
                                  <div className="np-chip-img np-chip-placeholder w-10 h-10 md:w-12 md:h-12"/>}
                              <span
                                  className="np-chip-title text-xs md:text-sm min-w-[120px] truncate flex-1">{li.title}</span>
                              <span className="np-chip-type text-[10px] md:text-xs">{li.type}</span>
                              <button type="button" className="np-chip-remove text-lg ml-auto"
                                      onClick={() => removeLinkedItem(li.externalId)}>×
                              </button>
                            </div>
                        ))}
                      </div>
                  )}

                  <AnimatePresence>
                    {searchOpen && (
                        <motion.div
                            initial={{opacity: 0, height: 0}}
                            animate={{opacity: 1, height: "auto"}}
                            exit={{opacity: 0, height: 0}}
                            className="np-search-panel border-t border-white/5 pt-3 overflow-hidden"
                            onSubmit={(e) => e.stopPropagation()}
                        >
                          {/* Veranderd naar grid-cols-3 zodat knoppen op mobile netjes schalen en niet afbreken */}
                          <div className="grid grid-cols-3 gap-1.5 md:gap-2 mb-3">
                            {QUERY_TYPES.map(({value, label, icon}) => (
                                <button
                                    key={value}
                                    type="button"
                                    className={`np-type-btn flex-col md:flex-row p-2 text-xs md:text-sm gap-1 ${queryType === value ? "np-type-btn--active" : ""}`}
                                    onClick={() => handleQueryTypeChange(value)}
                                >
                                  <span className="np-type-icon text-sm md:text-base">{icon}</span>
                                  <span className="text-[11px] md:text-xs font-medium">{label}</span>
                                </button>
                            ))}
                          </div>

                          {queryType && (
                              <div className="w-full">
                                <SearchBar
                                    queryType={queryType}
                                    onSearch={handleSearch}
                                    showBackButton={false}
                                />

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
              </div>

              {/* Photos Sub-section */}
              <div className="np-field mt-4 lg:mt-auto">
                <div className="np-field-row flex justify-between items-center mb-1.5">
                  <label className="np-label">Photos</label>
                  <span className="np-img-count text-xs opacity-60">{form.mediaInstanceDTOSet.length}/3</span>
                </div>
                {form.mediaInstanceDTOSet.length < 3 && (
                    <button
                        type="button"
                        onDragOver={(e) => {
                          e.preventDefault();
                          setDragOver(true);
                        }}
                        onDragLeave={() => setDragOver(false)}
                        onDrop={(e) => {
                          e.preventDefault();
                          setDragOver(false);
                          if (e.dataTransfer.files) addImages(e.dataTransfer.files);
                        }}
                        onClick={() => fileInputRef.current?.click()}
                        className={`np-dropzone p-4 md:p-6 ${dragOver ? "np-dropzone--active" : ""}`}
                    >
                      <span className="np-dropzone-text text-xs md:text-sm">Click or drag & drop</span>
                    </button>
                )}
                <input ref={fileInputRef} type="file" accept="image/*" multiple className="hidden"
                       onChange={handleImageUpload}/>

                {imagePreviews.length > 0 && (
                    <div className="np-previews mt-2 flex gap-2 overflow-x-auto py-1">
                      {imagePreviews.map((src, i) => (
                          <div key={i} className="np-preview-wrap flex-shrink-0">
                            <img src={src} alt="preview" className="np-preview-img w-14 h-14 md:w-16 md:h-16"/>
                            <button type="button" onClick={() => removeImage(i)} className="np-preview-remove">×
                            </button>
                          </div>
                      ))}
                    </div>
                )}
              </div>
            </div>

            {/* ACTIONS ROW */}
            <div
                className="lg:col-span-12 flex flex-col-reverse sm:flex-row items-center justify-end gap-3 border-t border-white/10 pt-4 mt-2">
              {error &&
                  <p className="np-error text-xs md:text-sm text-red-500 mr-auto w-full sm:w-auto mb-2 sm:mb-0">{error}</p>}
              <div className="flex w-full sm:w-auto items-center justify-end gap-3">
                <button type="button" onClick={onClose} className="np-cancel-btn px-4 py-2">Cancel</button>
                <button type="submit" disabled={loading}
                        className="np-submit-btn w-full sm:w-auto min-w-[120px] px-4 py-2 text-sm">
                  {loading ? "Posting..." : "Create Post"}
                </button>
              </div>
            </div>

          </form>
        </div>
      </Modal>
  );
}