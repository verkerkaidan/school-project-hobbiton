"use client"

import React, { createContext, useCallback, useContext, useEffect, useRef, useState } from "react";
import {createPortal} from "react-dom";
import { X, Check } from "lucide-react";


type ToastType = "success" | "error";

interface ErrorToastEntry {
    id: number;
    title: string;
    message: string;
    type: ToastType;
}

interface ErrorToastContextValue {
    showErrorToast: (title: string, message: string, type: ToastType) => void;
}

const ErrorToastContext = createContext<ErrorToastContextValue | null>(null);

export function ErrorToastProvider({children}:{children: React.ReactNode}) {
    const [errorToasts, setErrorToasts] = useState<ErrorToastEntry[]>([]);
    const counterRef = useRef(0);

    const showErrorToast = useCallback((title: string, message: string, type: ToastType) => {
        const id = ++counterRef.current;
        setErrorToasts((prev) => [...prev, { id, title, message, type }]);
    }, []);

    const removeErrorToast = useCallback((id: number) => {
        setErrorToasts((prev) => prev.filter((t) => t.id !== id));
    }, []);

    return (
        <ErrorToastContext.Provider value={{ showErrorToast }}>
            {children}
            <ToastStack toasts={errorToasts} onRemove={removeErrorToast} />
        </ErrorToastContext.Provider>
    );
}

export function useErrorToast(): ErrorToastContextValue {
    const ctx = useContext(ErrorToastContext);
    if (!ctx) throw new Error("useToast must be used within a ToastProvider");
    return ctx;
}

// ─── Portal stack ────────────────────────────────────────────────────────────

function ToastStack({
                        toasts,
                        onRemove,
                    }: {
    toasts: ErrorToastEntry[];
    onRemove: (id: number) => void;
}) {
    const [mounted, setMounted] = useState(false);

    // Wait for the DOM to be available (SSR safety)
    useEffect(() => {
        setMounted(true);
    }, []);

    if (!mounted) return null;

    return createPortal(
        <div
            aria-live="polite"
            aria-atomic="false"
            className="fixed bottom-6 left-1/2 -translate-x-1/2 z-9999 flex flex-col items-center gap-2 pointer-events-none"
        >
            {toasts.map((t) => (
                <ToastItem key={t.id} {...t} onRemove={onRemove} />
            ))}
        </div>,
        document.body
    );
}

function ToastItem({
                       id,
                       title,
                       message,
                       type,
                       onRemove,
                   }: {
    id: number;
    title: string;
    message: string;
    type: ToastType;
    onRemove: (id: number) => void;
}) {
    const isSuccess = type === "success";

    useEffect(() => {
        const timer = setTimeout(() => onRemove(id), 3000);
        return () => clearTimeout(timer);
    }, [id, onRemove]);

    return (
        <div
            role="alert"
            className={`
                flex items-start gap-3
                px-4 py-3 rounded-xl
                border shadow-lg backdrop-blur-sm
                animate-fade-in-up pointer-events-auto
                min-w-[320px] max-w-md overflow-hidden
                ${
                isSuccess
                    ? "bg-emerald-500/15 border-emerald-400/50"
                    : "bg-red-500/15 border-red-400/50"
            }
            `}
        >
            {/* Icon */}
            <div
                className={`
                    mt-0.5 shrink-0
                    ${isSuccess ? "text-emerald-300" : "text-red-300"}
                `}
            >
                {isSuccess ? (
                    <Check className="h-5 w-5" />
                ) : (
                    <X className="h-5 w-5" />
                )}
            </div>

            {/* Content */}
            <div className="flex flex-col min-w-0">
                {title && (
                    <div
                        className={`
                            text-base font-bold leading-tight
                            ${
                            isSuccess
                                ? "text-emerald-300"
                                : "text-red-300"
                        }
                        `}
                    >
                        {title}
                    </div>
                )}

                {message != "" && (
                    <div className="mt-1 text-sm text-white/80 wrap-break-word">
                        {message}
                    </div>
                )}
            </div>
        </div>
    );
}
