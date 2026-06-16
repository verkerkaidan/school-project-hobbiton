"use client";

import React, {useEffect, useRef, useState} from "react";
import {reportFile} from "@/lib/reportClient";
import {ContentFlag} from "@/types/enums";
import {useErrorToast} from "@/components/ui/ErrorToastProvider";

export function ImageMenu({ userId, relevantId, contentFlag }: { userId: string, relevantId: string, contentFlag: ContentFlag }) {
    const [open, setOpen] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const ref = useRef<HTMLDivElement>(null);
    const { showErrorToast } = useErrorToast();

    // close on outside click
    useEffect(() => {
        function handleClickOutside(e: MouseEvent) {
            if (ref.current && !ref.current.contains(e.target as Node)) {
                setOpen(false);
            }
        }
        document.addEventListener("mousedown", handleClickOutside);
        return () => document.removeEventListener("mousedown", handleClickOutside);
    }, []);

    const handleReport = async () => {
        try {
            setSubmitting(true);
            await reportFile(userId, relevantId);
            setOpen(false);
            showErrorToast("Report submitted", "", "success")
        } catch (e) {
            const message = e instanceof Error ? e.message : "Something went wrong";
            showErrorToast("Failed to submit report", message, "error")
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <div ref={ref} className="relative">
            {/* 3 dots button */}
            <button
                onClick={(e) => {
                    e.stopPropagation()
                    setOpen((prev) => !prev)
                }}
                className="bg-black/60 backdrop-blur-md text-white px-2 py-1 rounded-full hover:bg-black/80 transition cursor-pointer"
            >
                ⋯
            </button>

            {open && (
                <div className="
                    absolute top-full right-0 mt-2 w-44
                    rounded-xl
                    bg-neutral-900
                    border border-white/10
                    shadow-lg
                    p-1
                    z-30
                ">
                    <button
                        onClick={(e) => {
                            e.stopPropagation()
                            handleReport().then(() => setOpen(false))
                        }}
                        disabled={submitting}
                        className="
                            w-full flex items-center gap-3
                            px-4 py-3
                            text-sm text-red-400
                            rounded-lg
                            transition-colors
                            hover:bg-red-500/10
                            hover:text-red-300
                            disabled:opacity-50
                            disabled:cursor-not-allowed
                            cursor-pointer
                        "
                    >
                        <svg
                            xmlns="http://www.w3.org/2000/svg"
                            width="16"
                            height="16"
                            viewBox="0 0 24 24"
                            fill="none"
                            stroke="currentColor"
                            strokeWidth="2"
                            strokeLinecap="round"
                            strokeLinejoin="round"
                            className="h-4 w-4 shrink-0"
                        >
                            <path d="M4 15s1-1 4-1 5 2 8 2 4-1 4-1V3s-1 1-4 1-5-2-8-2-4 1-4 1z" />
                            <line x1="4" y1="22" x2="4" y2="15" />
                        </svg>

                        <span>
                            {submitting
                                ? "Reporting..."
                                : contentFlag === ContentFlag.RISK
                                    ? "Request review"
                                    : "Report"}
                        </span>
                    </button>
                </div>
            )}
        </div>
    );
}