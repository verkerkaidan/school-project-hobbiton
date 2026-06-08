"use client";
import { useState, useRef } from "react";
import { Modal } from "./Modal";
import { useRouter } from "next/navigation";
import { GymExercise } from "../createPost/GymExercise";
import { ReadingFields } from "../createPost/ReadingFields";
import { postCreateRequest } from "@/lib/graphQlClient";
import {
  BaseCreatePost,
  CreateGymPost,
  CreatePost,
  CreateReadingPost,
  CreateOtherPost,
  ModalProps,
} from "@/types/interfaces";
import Cookies from "js-cookie";
import { Post } from "@/types/posts";

type PostType = "GYM" | "READING" | "OTHER";

interface Exercise {
  title: string;
  reps: number;
  weight: number;
  rpe: number;
}

const BASE_DEFAULTS: BaseCreatePost = {
  title: "",
  description: "",
  totalDuration: 0,
  userId: "",
  mediaInstanceDTOSet: [],
};

const GYM_DEFAULTS: CreateGymPost = {
  ...BASE_DEFAULTS,
  type: "GYM",
  excercises: [],
};

const READING_DEFAULTS: CreateReadingPost = {
  ...BASE_DEFAULTS,
  bookTitle: "",
  type: "READING",
  pagesRead: 0,
  totalPagesRead: 0,
  bookFinished: false,
};

const OTHER_DEFAULTS: CreateOtherPost = {
  ...BASE_DEFAULTS,
  type: "OTHER",
};

export function CreatePostModal({ isOpen, onClose }: ModalProps) {
  const [form, setForm] = useState<CreatePost>(GYM_DEFAULTS);
  const [error, setError] = useState<string>("");
  const [loading, setLoading] = useState(false);
  const [imagePreviews, setImagePreviews] = useState<string[]>([]);
  const [dragOver, setDragOver] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);
  const router = useRouter();

  /**
   * When type changes, the form also changes directly
   */
  const handleTypeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const newType = e.target.value as PostType;
    // Reset to defaults for the new type, preserving shared base fields
    if (newType === "GYM") {
      setForm({
        ...GYM_DEFAULTS,
        title: form.title,
        description: form.description,
        totalDuration: form.totalDuration,
      });
    } else if (newType === "READING") {
      setForm({
        ...READING_DEFAULTS,
        title: form.title,
        description: form.description,
        totalDuration: form.totalDuration,
      });
    } else if (newType === "OTHER") {
      setForm({
        ...OTHER_DEFAULTS
      });
    } else {
      setForm({
        ...OTHER_DEFAULTS
      });
    }
    setError("");
  };

  /**
   * Gets called when form changes, so global items keep what they have
   */
  const handleBaseChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
  ) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
    setError("");
  };

  const handleGymChange = (
    index: number,
    field: keyof Exercise,
    value: string,
  ) => {
    if (form.type !== "GYM") return;
    const updated = form.excercises.map((ex, i) =>
      i === index
        ? { ...ex, [field]: field === "title" ? value : Number(value) }
        : ex,
    );
    setForm({ ...form, excercises: updated });
    setError("");
  };

  /**
   * Gets called when user wants to add another exercise
   * @returns void
   */
  const addExercise = () => {
    if (form.type !== "GYM") return;
    setForm({
      ...form,
      excercises: [
        ...form.excercises,
        { title: "", reps: 0, weight: 0, rpe: 0 },
      ],
    });
  };

  /**
   * Removes 1 item from list of Exercises
   * @param index Index of item to be removed
   * @returns void
   */
  const removeExercise = (index: number) => {
    if (form.type !== "GYM") return;
    setForm({
      ...form,
      excercises: form.excercises.filter((_, i) => i !== index),
    });
  };

  /**
   * When change is made in a field, this always triggers to make sure it is type string | number or boolean & updates the form
   * @returns
   */
  const handleReadingChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (form.type !== "READING") return;
    const { name, value, type, checked } = e.target;

    let parsed: string | number | boolean = value;
    if (type === "checkbox") parsed = checked;
    else if (type === "number") parsed = Number(value);

    setForm({ ...form, [name]: parsed });
    setError("");
  };

  /**
   * On submit, validates the entire form for errors
   */
  const validate = (): boolean => {
    if (!form.title.trim()) {
      setError("Title is required.");
      return false;
    }
    if (!form.description.trim()) {
      setError("Description is required.");
      return false;
    }
    if (!form.totalDuration || form.totalDuration <= 0) {
      setError("Total duration must be greater than 0.");
      return false;
    }

    if (form.type === "GYM") {
      if (form.excercises.length === 0) {
        setError("Add at least one exercise.");
        return false;
      }
      for (const [i, ex] of form.excercises.entries()) {
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

    if (form.type === "READING") {
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

  /**
   * Triggers when user clicks post, creates the post
   * @returns void
   */
  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!validate()) return;

    form.userId = Cookies.get("userId")!;
    try {
      setLoading(true);
      await postCreateRequest(form);
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
    setForm((prev) => ({
      ...prev,
      mediaInstanceDTOSet: [...prev.mediaInstanceDTOSet, ...toAdd],
    }));
    setImagePreviews((prev) => [...prev, ...newPreviews]);
    if (arr.length > remaining) {
      setError(
        `Only ${remaining} more image${remaining === 1 ? "" : "s"} can be added (max 3).`,
      );
    }
  };

  const handleImageUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) addImages(e.target.files);
    if (fileInputRef.current) fileInputRef.current.value = "";
  };

  const handleImageDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragOver(false);
    if (e.dataTransfer.files) addImages(e.dataTransfer.files);
  };

  const removeImage = (index: number) => {
    URL.revokeObjectURL(imagePreviews[index]);
    setForm((prev) => ({
      ...prev,
      mediaInstanceDTOSet: prev.mediaInstanceDTOSet.filter(
        (_, i) => i !== index,
      ),
    }));
    setImagePreviews((prev) => prev.filter((_, i) => i !== index));
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose}>
      <h2 className="text-xl font-bold mb-4">Create Post</h2>
      <form onSubmit={handleSubmit} className="flex flex-col gap-4">
        {/* Post type */}
        <div className="flex flex-col gap-1">
          <label className="text-sm font-medium text-white">Post type</label>
          <select
            value={form.type}
            onChange={handleTypeChange}
            className="border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand"
          >
            <option className="text-black" value="GYM">
              Gym
            </option>
            <option className="text-black" value="READING">
              Reading
            </option>
            <option className="text-black" value="OTHER">
              Other
            </option>
          </select>
        </div>

        {/* Shared base fields */}
        <div className="flex flex-col gap-1">
          <label className="text-sm font-medium text-white">Title</label>
          <input
            name="title"
            type="text"
            placeholder="Post title"
            value={form.title}
            onChange={handleBaseChange}
            className="border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand"
          />
        </div>

        <div className="flex flex-col gap-1">
          <label className="text-sm font-medium text-white">Description</label>
          <textarea
            name="description"
            placeholder="How did it go?"
            value={form.description}
            onChange={handleBaseChange}
            rows={3}
            className="border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand resize-none"
          />
        </div>

        <div className="flex flex-col gap-1">
          <label className="text-sm font-medium text-white">
            Total duration (minutes)
          </label>
          <input
            name="totalDuration"
            type="number"
            min={0}
            step={0.25}
            placeholder="55.25"
            value={form.totalDuration || ""}
            onChange={handleBaseChange}
            className="border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-brand"
          />
        </div>

        {/* GYM-specific fields */}
        {form.type === "GYM" && (
          <div className="flex flex-col gap-2 overflow-y-auto max-h-48 pr-1">
            <label className="text-sm font-medium text-white">Exercises</label>
            {form.excercises.map((ex, i) => (
              <GymExercise
                key={i}
                ex={ex}
                index={i}
                onRemove={removeExercise}
                onChange={handleGymChange}
              />
            ))}
            <button
              type="button"
              onClick={addExercise}
              className="text-sm text-brand border border-brand rounded px-3 py-2 hover:bg-brand hover:text-white transition-colors duration-200"
            >
              + Add exercise
            </button>
          </div>
        )}

        {/* READING-specific fields */}
        {form.type === "READING" && (
          <ReadingFields form={form} onChange={handleReadingChange} />
        )}

        {/* Media upload */}
        <div className="flex flex-col gap-2">
          <div className="flex items-center justify-between">
            <label className="text-sm font-medium text-white">Photos</label>
            <span className="text-xs text-white/40">
              {form.mediaInstanceDTOSet.length}/3
            </span>
          </div>

          {form.mediaInstanceDTOSet.length < 3 && (
            <button
              type="button"
              onDragOver={(e) => {
                e.preventDefault();
                setDragOver(true);
              }}
              onDragLeave={() => setDragOver(false)}
              onDrop={handleImageDrop}
              onClick={() => fileInputRef.current?.click()}
              className={`w-full flex flex-col items-center gap-2 py-5 rounded-2xl border-2 border-dashed transition-colors cursor-pointer
                                ${
                                  dragOver
                                    ? "border-brand/60 bg-brand/10"
                                    : "border-white/15 hover:border-brand/40"
                                }`}
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="22"
                height="22"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="1.5"
                strokeLinecap="round"
                strokeLinejoin="round"
                className="text-accent"
              >
                <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                <polyline points="17 8 12 3 7 8" />
                <line x1="12" y1="3" x2="12" y2="15" />
              </svg>
              <span className="text-sm text-accent">
                Click or drag &amp; drop to upload
              </span>
              <span className="text-xs text-white/30">
                PNG &middot; JPG &middot; WEBP &middot; max 3
              </span>
            </button>
          )}

          <input
            ref={fileInputRef}
            type="file"
            accept="image/png,image/jpeg,image/webp,image/gif"
            multiple
            className="hidden"
            onChange={handleImageUpload}
          />

          {imagePreviews.length > 0 && (
            <div className="flex gap-2 flex-wrap">
              {imagePreviews.map((src, i) => (
                <div key={i} className="relative group w-20 h-20">
                  <img
                    src={src}
                    alt={`preview-${i}`}
                    className="w-20 h-20 object-cover rounded-xl border border-white/10"
                  />
                  <button
                    type="button"
                    onClick={() => removeImage(i)}
                    className="absolute top-1 right-1 w-5 h-5 rounded-full bg-black/70 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity text-white hover:bg-red-500/80"
                    aria-label="Remove image"
                  >
                    <svg
                      xmlns="http://www.w3.org/2000/svg"
                      width="10"
                      height="10"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      strokeWidth="2.5"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    >
                      <line x1="18" y1="6" x2="6" y2="18" />
                      <line x1="6" y1="6" x2="18" y2="18" />
                    </svg>
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>

        {error && <p className="text-red-500 text-sm">{error}</p>}

        <div className="flex gap-2 pt-1">
          <button
            type="submit"
            disabled={loading}
            className="flex-1 bg-brand text-white py-2 rounded hover:opacity-90 transition-opacity duration-200"
          >
            {loading ? "Posting..." : "Create post"}
          </button>
          <button
            type="button"
            onClick={onClose}
            className="text-sm text-gray-400 hover:text-gray-600 px-3"
          >
            Cancel
          </button>
        </div>
      </form>
    </Modal>
  );
}
