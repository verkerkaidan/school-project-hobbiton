"use client"

import { createContext, useContext, useState, useEffect } from "react";

interface Toast {
    message: string;
    type: "success" | "error";
}

interface ProfilePicContextValue {
    imageSrc: string | undefined;
    setImageSrc: (src: string | undefined) => void;
    showToast: (toast: Toast) => void;
}

export const ProfilePicContext = createContext<ProfilePicContextValue | undefined>(undefined);

export function ProfilePicProvider({ children }: { children: React.ReactNode }) {
    const [imageSrc, setImageSrc] = useState<string | undefined>(undefined);
    const [toast, setToast] = useState<Toast | null>(null);

    function showToast(t: Toast) {
        setToast(t);
    }

    return (
        <ProfilePicContext.Provider value={{ imageSrc, setImageSrc, showToast }}>
            {children}
            {toast && (
                <Toast
                    message={toast.message}
                    type={toast.type}
                    onDone={() => setToast(null)}
                />
            )}
        </ProfilePicContext.Provider>
    );
}

export function useProfilePic() {
    const ctx = useContext(ProfilePicContext);
    if (!ctx) throw new Error("useProfilePic must be used within a ProfilePicProvider");
    return ctx;
}

function Toast({
   message,
   type,
   onDone,
}: {
    message: string;
    type: "success" | "error";
    onDone: () => void;
}) {
    useEffect(() => {
        const timer = setTimeout(onDone, 3000);
        return () => clearTimeout(timer);
    }, []);

    const isSuccess = type === "success";

    return (
        <div
            className={`
        fixed bottom-6 left-1/2 -translate-x-1/2
        px-9 py-2 rounded-xl text-sm font-semibold
        border transition-colors animate-fade-in-up
        ${isSuccess
                ? "bg-brand/30 border-brand/20 text-brand"
                : "bg-red-500/30 border-red-500/30 text-red-400"
            }
      `}
        >
            {message}
        </div>
    );
}