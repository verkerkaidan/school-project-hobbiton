"use client";
import { useState } from "react";
import { Modal } from "./Modal";
import { useRouter } from "next/navigation";
import { GymExercise } from "../createPost/GymExercise";
import { ReadingFields } from "../createPost/ReadingFields";
import { postCreateRequest } from "@/lib/graphQlClient";
import { BaseCreatePost, CreateGymPost, CreatePost, CreateReadingPost, ModalProps } from "@/types/interfaces";
import Cookies from "js-cookie";
import { Post } from "@/types/posts";

type PostType = "GYM" | "READING";

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
    mediaInstanceSet: [],
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

export function CreatePostModal({ isOpen, onClose }: ModalProps) {
    const [form, setForm] = useState<CreatePost>(GYM_DEFAULTS);
    const [error, setError] = useState<string>("");
    const [loading, setLoading] = useState(false);
    const router = useRouter();

    /**
     * When type changes, the form also changes directly
     */
    const handleTypeChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        const newType = e.target.value as PostType;
        // Reset to defaults for the new type, preserving shared base fields
        if (newType === "GYM") {
            setForm({ ...GYM_DEFAULTS, title: form.title, description: form.description, totalDuration: form.totalDuration });
        } else {
            setForm({ ...READING_DEFAULTS, title: form.title, description: form.description, totalDuration: form.totalDuration });
        }
        setError("");
    };

    /**
     * Gets called when form changes, so global items keep what they have
     */
    const handleBaseChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
        setError("");
    };


    const handleGymChange = (index: number, field: keyof Exercise, value: string) => {
        if (form.type !== "GYM") return;
        const updated = form.excercises.map((ex, i) =>
            i === index ? { ...ex, [field]: field === "title" ? value : Number(value) } : ex
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
        setForm({ ...form, excercises: [...form.excercises, { title: "", reps: 0, weight: 0, rpe: 0 }] });
    };

    /**
     * Removes 1 item from list of Exercises
     * @param index Index of item to be removed
     * @returns void
     */
    const removeExercise = (index: number) => {
        if (form.type !== "GYM") return;
        setForm({ ...form, excercises: form.excercises.filter((_, i) => i !== index) });
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
        if (!form.title.trim()) { setError("Title is required."); return false; }
        if (!form.description.trim()) { setError("Description is required."); return false; }
        if (!form.totalDuration || form.totalDuration <= 0) { setError("Total duration must be greater than 0."); return false; }

        if (form.type === "GYM") {
            if (form.excercises.length === 0) { setError("Add at least one exercise."); return false; }
            for (const [i, ex] of form.excercises.entries()) {
                if (!ex.title.trim()) { setError(`Exercise ${i + 1}: title is required.`); return false; }
                if (ex.reps <= 0) { setError(`Exercise ${i + 1}: reps must be greater than 0.`); return false; }
                if (ex.weight <= 0) { setError(`Exercise ${i + 1}: weight must be greater than 0.`); return false; }
                if (ex.rpe < 1 || ex.rpe > 10) { setError(`Exercise ${i + 1}: RPE must be between 1 and 10.`); return false; }
            }
        }

        if (form.type === "READING") {
            if (!form.bookTitle.trim()) { setError("Book title is required."); return false; }
            if (form.pagesRead <= 0) { setError("Pages read must be greater than 0."); return false; }
            if (form.totalPagesRead <= 0) { setError("Total pages must be greater than 0."); return false; }
            if (form.pagesRead > form.totalPagesRead) { setError("Pages read cannot exceed total pages."); return false; }
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
            router.push("/profile")
            onClose();
        } catch (err) {
            setError(err instanceof Error ? err.message : "Creation of post failed.");
        } finally {
            setLoading(false);
        }
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
                        <option className="text-black" value="GYM">Gym</option>
                        <option className="text-black" value="READING">Reading</option>
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
                    <label className="text-sm font-medium text-white">Total duration (minutes)</label>
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