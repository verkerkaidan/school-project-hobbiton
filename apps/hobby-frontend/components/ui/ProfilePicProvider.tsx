"use client"

import { createContext, useContext, useState } from "react";
import {useErrorToast} from "@/components/ui/ErrorToastProvider";

interface ProfilePicContextValue {
    imageSrc: string | undefined;
    setImageSrc: (src: string | undefined) => void;
    showToast: (title: string, message: string, type: "success" | "error") => void;
}

export const ProfilePicContext = createContext<ProfilePicContextValue | undefined>(undefined);

export function ProfilePicProvider({ children }: { children: React.ReactNode }) {
    const [imageSrc, setImageSrc] = useState<string | undefined>(undefined);
    const { showErrorToast } = useErrorToast();

    function showToast(title: string, message: string, type: "success" | "error") {
        showErrorToast(title, message, type);
    }

    return (
        <ProfilePicContext.Provider value={{ imageSrc, setImageSrc, showToast }}>
            {children}
        </ProfilePicContext.Provider>
    );
}

export function useProfilePic() {
    const ctx = useContext(ProfilePicContext);
    if (!ctx) throw new Error("useProfilePic must be used within a ProfilePicProvider");
    return ctx;
}