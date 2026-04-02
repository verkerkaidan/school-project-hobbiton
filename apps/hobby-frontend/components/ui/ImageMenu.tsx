import { useState, useRef, useEffect } from "react";
import { reportFile } from "@/lib/reportClient";

export function ImageMenu({ userId, relevantId }: { userId: string, relevantId: string }) {
    const [open, setOpen] = useState(false);
    const ref = useRef<HTMLDivElement>(null);

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
            await reportFile(userId, relevantId);
            alert("Reported");
        } catch {
            alert("Failed");
        } finally {
            setOpen(false);
        }
    };

    return (
        <div ref={ref} className="relative">
            {/* 3 dots button */}
            <button
                onClick={() => setOpen((prev) => !prev)}
                className="bg-black/60 backdrop-blur-md text-white px-2 py-1 rounded-full hover:bg-black/80 transition"
            >
                ⋯
            </button>

            {/* popup */}
            {open && (
                <div className="absolute right-0 mt-2 w-48 rounded-xl bg-neutral-900 text-white shadow-lg border border-white/10 backdrop-blur-md p-2 animate-in fade-in zoom-in-95">
                    <button
                        onClick={handleReport}
                        className="w-full text-left px-3 py-2 rounded-lg hover:bg-white/10 transition"
                    >
                        Report
                    </button>

                </div>
            )}
        </div>
    );
}