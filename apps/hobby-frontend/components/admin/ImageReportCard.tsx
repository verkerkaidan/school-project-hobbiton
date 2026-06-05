import React, {useEffect, useRef, useState} from "react";
import type {FileReport, ReportedFile} from "@/types/reports";
import {motion} from "framer-motion";
import {ContentFlag} from "@/types/enums";
import {ChevronDown, Trash2} from "lucide-react";
import {deleteMediaFile, updateContentFlag} from "@/lib/reportClient";

interface ImageReportCardProps {
    item: ReportedFile;
    index: number;
    onRemove: () => void;
}

const CONTENT_FLAG_CONFIG: Record<ContentFlag, { label: string }> = {
    [ContentFlag.RISK]: { label: "Risk" },
    [ContentFlag.SAFE]: { label: "Safe" },
    [ContentFlag.UNASSIGNED]: {label: "Unassigned"}
};

const ADMIN_NOTE: FileReport = {
    note: "",
    hash: "",
    contentFlag: ContentFlag.UNASSIGNED
};

const ALL_FLAGS = Object.values(ContentFlag).filter(flag => flag !== ContentFlag.UNASSIGNED);

export const ImageReportCard = ({ item, index, onRemove }: ImageReportCardProps) => {
    const [revealed, setRevealed] = useState(false);
    const [tempContentFlag, setTempContentFlag] = useState(ContentFlag.UNASSIGNED);
    const [form, setForm] = useState<FileReport>(ADMIN_NOTE);
    const [deleteModalOpen, setDeleteModal] = useState<boolean>(false);
    const [submitting, setSubmitting] = useState(false);
    const [submitError, setSubmitError] = useState("");
    const [toast, setToast] = useState<{ message: string; type: "success" | "error" } | null>(null);
    const [modalOpen, setModalOpen] = useState(false);
    const [scale, setScale] = useState(1);
    const [offset, setOffset] = useState({ x: 0, y: 0 });
    const [isDragging, setIsDragging] = useState(false);
    const dragStart = useRef<{ x: number; y: number; ox: number; oy: number } | null>(null);
    const lastPinchDist = useRef<number | null>(null);
    const imgRef = useRef<HTMLDivElement>(null);

    const imageSrc = `data:${item.contentType};base64,${item.file}`;

    // Close modal on Escape
    useEffect(() => {
        if (!modalOpen) return;
        const onKey = (e: KeyboardEvent) => { if (e.key === "Escape") closeModal(); };
        window.addEventListener("keydown", onKey);
        return () => window.removeEventListener("keydown", onKey);
    }, [modalOpen]);

    useEffect(() => {
        const hash = item.hash;
        setForm({...form, hash: hash, contentFlag: tempContentFlag});
    }, [tempContentFlag]);

    function openModal() {
        setModalOpen(true);
        setScale(2);
        setOffset({ x: 0, y: 0 });
    }

    function closeModal() {
        setModalOpen(false);
        setScale(1);
        setOffset({ x: 0, y: 0 });
    }

    // Scroll to zoom
    function onWheel(e: React.WheelEvent) {
        e.preventDefault();
        setScale(s => Math.min(8, Math.max(1, s - e.deltaY * 0.001)));
    }

    // Mouse drag
    function onMouseDown(e: React.MouseEvent) {
        if (scale === 1) return;
        setIsDragging(true);
        dragStart.current = { x: e.clientX, y: e.clientY, ox: offset.x, oy: offset.y };
    }

    function onMouseMove(e: React.MouseEvent) {
        if (!isDragging || !dragStart.current) return;
        setOffset({
            x: dragStart.current.ox + (e.clientX - dragStart.current.x),
            y: dragStart.current.oy + (e.clientY - dragStart.current.y),
        });
    }

    function onMouseUp() {
        setIsDragging(false);
        dragStart.current = null;
    }

    // Touch pinch-to-zoom + drag
    function onTouchMove(e: React.TouchEvent) {
        if (e.touches.length === 2) {
            e.preventDefault();
            const dx = e.touches[0].clientX - e.touches[1].clientX;
            const dy = e.touches[0].clientY - e.touches[1].clientY;
            const dist = Math.sqrt(dx * dx + dy * dy);
            if (lastPinchDist.current !== null) {
                const delta = dist - lastPinchDist.current;
                setScale(s => Math.min(8, Math.max(1, s + delta * 0.01)));
            }
            lastPinchDist.current = dist;
        } else if (e.touches.length === 1 && isDragging && dragStart.current) {
            setOffset({
                x: dragStart.current.ox + (e.touches[0].clientX - dragStart.current.x),
                y: dragStart.current.oy + (e.touches[0].clientY - dragStart.current.y),
            });
        }
    }

    function onTouchStart(e: React.TouchEvent) {
        if (e.touches.length === 1 && scale > 1) {
            setIsDragging(true);
            dragStart.current = { x: e.touches[0].clientX, y: e.touches[0].clientY, ox: offset.x, oy: offset.y };
        }
        lastPinchDist.current = null;
    }

    function onTouchEnd() {
        setIsDragging(false);
        dragStart.current = null;
        lastPinchDist.current = null;
    }

    // Reset zoom on double-click
    function onDoubleClick() {
        setScale(1);
        setOffset({ x: 0, y: 0 });
    }

    const handleBaseChange = (
        e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>,
    ) => {
        setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }));
        setSubmitError("");
    };

    async function onSubmitContentFlag() {
        if (!validate()) return;
        try {
            setSubmitting(true)
            await updateContentFlag(form);
            setToast({ message: "Content flag updated successfully", type: "success" });
            setTimeout(onRemove, 3000)
        } catch (error) {
            setToast({ message: "Failed to update content flag", type: "error" });
        } finally {
            setSubmitting(false);
        }
    }

    function validate(): boolean {
        if (form.contentFlag === ContentFlag.UNASSIGNED) {
            setSubmitError("Please select a valid content flag before submitting");
            return false;
        }
        else if (form.note === "") {
            setSubmitError("Please note why this content flag is being assigned");
            return false;
        }
        else if (form.note.length < 10) {
            setSubmitError("Please provide a longer note");
            return false;
        }
        return true;
    }

    async function handleDeletion() {
        try {
            setSubmitting(true)
            await deleteMediaFile(item.hash);
            setToast({ message: "File was deleted", type: "success" });
            setTimeout(onRemove, 3000)
        } catch (error) {
            setToast({ message: "Failed to delete media file", type: "error" });
        } finally {
            setSubmitting(false);
            setDeleteModal(false);
        }
    }

    return (
        <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            whileInView={{ opacity: 1, scale: 1 }}
            viewport={{ once: true, amount: 0.3 }}
            transition={{ duration: 0.5, delay: index * 0.05 }}
            className="break-inside-avoid group mb-6 rounded-xl overflow-hidden ring-1 ring-brand/30 shadow-[0_0_20px_rgba(236,72,153,0.12)]"
        >
            {/* Card */}
            <div className="relative w-full rounded-xl overflow-hidden">
                <div className="relative w-full rounded-lg overflow-hidden bg-black/10 flex items-center justify-center max-h-[600px]">
                    <img
                        src={imageSrc}
                        alt={`Reported file ${index + 1}`}
                        className={`object-contain max-w-full max-h-[600px] w-auto h-auto transition-opacity ${revealed ? "cursor-zoom-in" : ""}`}
                        onClick={revealed ? openModal : undefined}
                    />
                    <div className="absolute inset-0 bg-linear-to-t from-background/90 via-transparent to-transparent opacity-60 pointer-events-none" />

                    {!revealed && (
                        <div
                            onClick={(e) => e.stopPropagation()}
                            className="absolute inset-0 z-20 flex flex-col items-center justify-center backdrop-blur-xl text-white text-center p-4"
                        >
                            <div className="max-w-xs">
                                <h3 className="text-lg font-semibold mb-2">Content Warning</h3>
                                <p className="text-sm opacity-80 mb-4">
                                    This image may contain sensitive or disturbing content.
                                </p>
                                <button
                                    onClick={(e) => { e.stopPropagation(); setRevealed(true); }}
                                    className="px-4 py-2 bg-white text-black rounded-full text-sm font-medium hover:bg-gray-200 transition"
                                >
                                    View Image
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            </div>

            {/* Admin input Area */}
            <div className="p-6 flex-col items-center justify-between">
                <h2 className="text-lg font-semibold">
                    Currently assigned flag:
                    <span className="text-brand ml-2">
                        {CONTENT_FLAG_CONFIG[item.contentFlag].label}
                    </span>
                </h2>

                {/* Note field */}
                <div className="flex flex-col gap-1 mt-6">
                    <label className="text-xs font-small text-white">Reason for assigned flag</label>
                    <input
                        name="note"
                        type="text"
                        placeholder="Reason for assigned flag"
                        value={form.note}
                        onChange={handleBaseChange}
                        className="appearance-none pl-2.5 pr-6 py-1.5 rounded-lg bg-white/5 border border-white/20 text-white/60 text-xs focus:outline-none focus:border-brand/40 hover:border-white/30 transition-colors cursor-pointer disabled:opacity-40"
                    />
                </div>

                <div className="flex items-center gap-2 justify-between mt-6">
                    {/* ContentFlag select */}
                    <div className="relative">
                        <select
                            value={tempContentFlag}
                            onChange={(e) => {
                                setTempContentFlag(e.target.value as ContentFlag);
                            }}
                            className="appearance-none pl-2.5 pr-6 py-1.5 rounded-lg bg-white/5 border border-white/20 text-white/60 text-xs focus:outline-none focus:border-brand/40 hover:border-white/30 transition-colors cursor-pointer disabled:opacity-40"
                            title="Change content flag"
                        >
                            <option value={ContentFlag.UNASSIGNED} disabled hidden>
                                {CONTENT_FLAG_CONFIG[ContentFlag.UNASSIGNED].label}
                            </option>
                            {ALL_FLAGS.map((flag) => (
                                <option key={flag} value={flag} className="bg-[#0d0d0f]">
                                    {CONTENT_FLAG_CONFIG[flag].label}
                                </option>
                            ))}
                        </select>
                        <ChevronDown className="absolute right-1.5 top-1/2 -translate-y-1/2 w-3 h-3 text-white/40 pointer-events-none" />
                    </div>

                    <div
                        className={`flex items-center gap-2`}
                    >
                        <button
                            onClick={onSubmitContentFlag}
                            className="appearance-none pl-2.5 pr-6 py-1.5 rounded-lg bg-white/5 border border-white/20 text-white/60 text-xs focus:outline-none focus:border-brand/40 hover:border-white/30 transition-colors cursor-pointer disabled:opacity-40"
                            disabled={submitting}
                        >
                            Submit contentFlag
                        </button>

                        {/* Delete posts button */}
                        <button
                            onClick={() => {
                                setDeleteModal(true)
                            }}
                            className="w-7 h-7 flex items-center justify-center rounded-lg text-white/30 hover:text-rose-400 hover:bg-rose-500/10 transition-colors"
                            title={`Ban image and delete all associated reports`}
                        >
                            <Trash2 className="w-3.5 h-3.5" />
                        </button>
                    </div>
                </div>

                {submitError != "" && (
                    <p className="!text-red-500 text-xs">{submitError}</p>
                )}
            </div>

            {/* Modal */}
            {modalOpen && (
                <div
                    className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm"
                    onClick={closeModal}
                >
                    {/* Close button */}
                    <button
                        className="absolute top-4 right-4 z-60 text-white bg-white/20 hover:bg-white/30 rounded-full w-9 h-9 flex items-center justify-center transition"
                        onClick={closeModal}
                    >
                        ✕
                    </button>

                    {/* Zoom hint */}
                    <div className="absolute bottom-4 left-1/2 -translate-x-1/2 text-white/50 text-xs select-none pointer-events-none">
                        Scroll or pinch to zoom · Drag to pan · Double-click to reset
                    </div>

                    {/* Zoom level badge */}
                    {scale > 1 && (
                        <div className="absolute top-4 left-4 text-white/60 text-xs bg-white/10 rounded px-2 py-1 select-none pointer-events-none">
                            {Math.round(scale * 100)}%
                        </div>
                    )}

                    {/* Image container */}
                    <div
                        ref={imgRef}
                        className="relative flex items-center justify-center w-full h-full overflow-hidden"
                        onClick={closeModal}
                    >
                        <img
                            src={imageSrc}
                            alt={`Reported file ${index + 1}`}
                            draggable={false}
                            onClick={(e) => e.stopPropagation()}
                            onWheel={onWheel}
                            onMouseDown={onMouseDown}
                            onMouseMove={onMouseMove}
                            onMouseUp={onMouseUp}
                            onMouseLeave={onMouseUp}
                            onTouchStart={onTouchStart}
                            onTouchMove={onTouchMove}
                            onTouchEnd={onTouchEnd}
                            onDoubleClick={onDoubleClick}
                            className="max-w-[90vw] max-h-[90vh] object-contain rounded-lg select-none"
                            style={{
                                transform: `scale(${scale}) translate(${offset.x / scale}px, ${offset.y / scale}px)`,
                                transition: isDragging ? "none" : "transform 0.15s ease",
                                cursor: scale > 1 ? (isDragging ? "grabbing" : "grab") : "zoom-in"
                            }}
                        />
                    </div>
                </div>
            )}
            {toast && (
                <Toast
                    message={toast.message}
                    type={toast.type}
                    onDone={() => setToast(null)}
                />
            )}
            {deleteModalOpen && (
                <DeleteModal
                    imageSrc={imageSrc}
                    handleDeletion={handleDeletion}
                    setDeleteModal={setDeleteModal}
                />
            )}
        </motion.div>
    );
};

function Toast({ message, type, onDone }: { message: string; type: "success" | "error"; onDone: () => void }) {
    useEffect(() => {
        const timer = setTimeout(onDone, 3000);
        return () => clearTimeout(timer);
    }, []);

    const isSuccess = type === "success";

    return (
        <div className={`
            fixed bottom-6 left-1/2 -translate-x-1/2
            px-9 py-2 rounded-xl text-sm font-semibold
            border transition-colors animate-fade-in-up
            ${isSuccess
            ? "bg-brand/30 border-brand/20 text-brand"
            : "bg-red-500/30 border-red-500/30 text-red-400"
        }
        `}>
            {message}
        </div>
    );
}

function DeleteModal(
    {imageSrc, handleDeletion, setDeleteModal}:
    {
        imageSrc: string,
        handleDeletion: () => void,
        setDeleteModal: any
    }) {

    const [deleting, setDeleting] = useState(false);

    return (
        <div
            className="fixed inset-0 z-50 flex flex-col items-center justify-center bg-black/30 backdrop-blur-sm"
            onClick={() => setDeleteModal(false)}
        >
            <img
                src={imageSrc}
                alt="File to be deleted"
                className="object-contain max-w-full max-h-[400px] w-auto h-auto"
            />

            <div className="flex items-center justify-between gap-8 mt-6">
                <button
                    onClick={(e) => {
                        e.stopPropagation()
                        setDeleteModal(false)
                    }}
                    disabled={deleting}
                    className="px-4 py-2.5 rounded-xl bg-rose-500/20 border border-rose-500/30 text-rose-400 hover:bg-rose-500/30 hover:text-rose-300 transition-colors text-sm font-medium disabled:opacity-50"
                >
                    Cancel
                </button>

                <button
                    onClick={(e) => {
                        e.stopPropagation();
                        setDeleting(true);
                        handleDeletion();
                    }}
                    disabled={deleting}
                    className="px-4 py-2.5 rounded-lg bg-white/5 border border-white/20 text-white/60 text-sm hover:border-white/30 transition-colors disabled:opacity-40"
                >
                    {deleting ? "Deleting…" : "Confirm Deletion"}
                </button>
            </div>
        </div>
    );
}