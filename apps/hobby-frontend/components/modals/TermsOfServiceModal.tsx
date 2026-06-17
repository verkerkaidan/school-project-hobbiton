"use client";
import { useRef, useState, useEffect } from "react";
import { Modal } from "./Modal";

interface TosSection {
    title: string;
    body: string;
}

const TOS_SECTIONS: TosSection[] = [
    {
        title: "Welcome to Hobbiton.",
        body: "By creating an account, accessing, or using our platform, you agree to these Terms of " +
            "Service (\"Terms\")."
    },
    {
        title: "1. ABOUT THE PLATFORM",
        body: "Hobbiton is a social platform that allows users to share and discuss hobbies, interests, activities, " +
            "and related content with others. " +
            "\nThese Terms govern your use of the platform, including content, features, and services provided " +
            "through it."
    },
    {
        title: "2. USER CONTENT",
        body: "You retain ownership of the content you post, including text, images, comments, and other materials." +
            "\nBy posting content, you grant Hobbiton a non-exclusive, worldwide, royalty-free license to store, " +
            "display, and distribute that content as necessary to operate the platform." +
            "\nYou are solely responsible for any content you upload or share." +
            "\nTo grant you successful access to the Hobbiton platform, we store Cookies on your device."
    },
    {
        title: "3. PROHIBITED CONTENT",
        body: "Users may not post, upload, share, or distribute content that:" +
            "\n  - Is illegal or promotes illegal activity." +
            "\n  - Harasses, threatens, intimidates, or abuses other users." +
            "\n  - Contains hate speech, discrimination, or targeted harassment." +
            "\n  - Violates another person's privacy or intellectual property rights." +
            "\n  - Contains or promotes harmful content, including content involving cars or other content " +
            "designated by the platform as harmful or inappropriate." +
            "\nContent warnings may be applied to certain content categories. A content warning does not imply " +
            "that such content is permitted on the platform. The lack of a content warning likewise does not " +
            "automatically imply that such content is permitted on the platform."
    },
    {
        title: "4. MODERATION AND ENFORCEMENT",
        body: "To maintain a safe and welcoming community, administrators and moderators may review content " +
            "posted on the platform. As such, administrators reserve the right to:" +
            "\n  - Remove or restrict access to any post, image, comment, profile, or other content." +
            "\n  - Edit or remove content warnings." +
            "\n  - Suspend or terminate user accounts." +
            "\n  - Take any moderation action deemed necessary to protect users or the platform." +
            "\nThese actions may be taken at the sole discretion of the administrators when content is " +
            "considered harmful, inappropriate, disruptive, or in any way a violation of these Terms."
    },
    {
        title: "5. REPORTING CONTENT",
        body: "Users may report content or behavior they believe violates these Terms." +
            "\nThe platform may investigate reports and take appropriate action, including content " +
            "removal or account suspension."
    },
    {
        title: "6. AVAILABILITY AND SERVICE",
        body: "The platform is provided on an \"as is\" and \"as available\" basis." +
            "\nBecause this project is developed, maintained, and operated by students, we do not guarantee:" +
            "\n  - Continuous availability." +
            "\n  - Error-free operation." +
            "\n  - Permanent storage of content." +
            "\n  - Compatibility with all devices or browsers." +
            "\nWe may modify, suspend, or discontinue any part of the platform at any time without notice."
    },
    {
        title: "7. LIMITATION AND LIABILITY",
        body: "To the fullest extent permitted by law, Hobbiton, its developers, and contributors shall not " +
            "be liable for any damages, losses, or claims arising from:" +
            "\n  - User-generated content." +
            "\n  - User interactions." +
            "\n  - Temporary or permanent service interruptions." +
            "\n  - Content removal or account suspension." +
            "\n  - Unauthorized access to user accounts or data."
    },
    {
        title: "8. CHANGES TO THESE TERMS",
        body: "We may update these Terms from time to time." +
            "\nContinued use of the platform after changes are posted constitutes acceptance of the updated Terms."
    },
];

const EDIT_DATE = '9th of june 2026'

interface TermsOfServiceModalProps {
    isOpen: boolean;
    onAccept: () => void;
}

export function TermsOfServiceModal({
                                        isOpen,
                                        onAccept,
                                    }: TermsOfServiceModalProps) {
    const [hasScrolledToBottom, setHasScrolledToBottom] = useState(false);
    const [checkedTos, setCheckedTos] = useState(false);
    const [checkedCars, setCheckedCars] = useState(false);
    const scrollRef = useRef<HTMLDivElement>(null);

    // Reset state whenever the modal opens
    useEffect(() => {
        if (isOpen) {
            setHasScrolledToBottom(false);
            setCheckedTos(false);
            setCheckedCars(false);
            // Reset scroll position
            if (scrollRef.current) {
                scrollRef.current.scrollTop = 0;
            }
        }
    }, [isOpen]);

    const handleScroll = () => {
        const el = scrollRef.current;
        if (!el) return;
        const atBottom = el.scrollTop + el.clientHeight >= el.scrollHeight - 8;
        if (atBottom) setHasScrolledToBottom(true);
    };

    const canSubmit = hasScrolledToBottom && checkedTos && checkedCars;

    return (
        <Modal isOpen={isOpen} onClose={() => {} } largeModal={true}>
            <h2 className="text-xl font-bold mb-1">Terms of Service</h2>
            <p className="text-sm text-gray-400 mb-3">
                Please read and accept our terms to continue.
            </p>

            {/* Scrollable TOS sections */}
            <div
                ref={scrollRef}
                onScroll={handleScroll}
                className="border border-gray-600 rounded-md h-56 overflow-y-auto p-3 text-sm text-gray-300 leading-relaxed bg-gray-900 mb-4 flex flex-col gap-4"
            >
                {TOS_SECTIONS.map((section, i) => {
                    // Split body into prose lines and bullet lines
                    const lines = section.body.split("\n");
                    const blocks: { type: "text" | "bullet"; content: string }[] = [];
                    for (const line of lines) {
                        const bullet = line.match(/^\s*-\s+(.*)/);
                        if (bullet) {
                            blocks.push({ type: "bullet", content: bullet[1] });
                        } else if (line.trim()) {
                            blocks.push({ type: "text", content: line });
                        }
                    }

                    return (
                        <div key={i}>
                            {section.title && (
                                <p className="font-semibold text-white mb-1">{section.title}</p>
                            )}
                            {blocks.map((block, j) =>
                                block.type === "bullet" ? null : (
                                    <p key={j} className="mb-1">{block.content}</p>
                                )
                            )}
                            {blocks.some((b) => b.type === "bullet") && (
                                <ul className="list-disc list-outside ml-4 flex flex-col gap-0.5 mt-1">
                                    {blocks
                                        .filter((b) => b.type === "bullet")
                                        .map((b, j) => (
                                            <li key={j}>{b.content}</li>
                                        ))}
                                </ul>
                            )}
                        </div>
                    );
                })}
            </div>

            {!hasScrolledToBottom && (
                <p className="text-xs text-amber-400 mb-3 flex items-center gap-1">
                    <span>↓</span> Scroll to the bottom to continue
                </p>
            )}

            <p className="italic text-xs text-gray-500 mb-4">
                Last updated: {EDIT_DATE}
            </p>

            {/* Checkboxes */}
            <div className="flex flex-col gap-3 mb-5">
                <label className="flex items-start gap-2 cursor-pointer select-none">
                    <input
                        type="checkbox"
                        checked={checkedTos}
                        onChange={(e) => setCheckedTos(e.target.checked)}
                        disabled={!hasScrolledToBottom}
                        className="mt-0.5 accent-brand disabled:opacity-40 cursor-pointer"
                    />
                    <span
                        className={`text-sm ${!hasScrolledToBottom ? "text-gray-500" : "text-gray-200"}`}
                    >
            I have read and agree to the terms and conditions of Hobbiton.
          </span>
                </label>

                <label className="flex items-start gap-2 cursor-pointer select-none">
                    <input
                        type="checkbox"
                        checked={checkedCars}
                        onChange={(e) => setCheckedCars(e.target.checked)}
                        disabled={!hasScrolledToBottom}
                        className="mt-0.5 accent-brand disabled:opacity-40 cursor-pointer"
                    />
                    <span
                        className={`text-sm ${!hasScrolledToBottom ? "text-gray-500" : "text-gray-200"}`}
                    >
            I understand that posting photos of cars will get my post flagged and my account reviewed.
          </span>
                </label>
            </div>

            {/* Submit */}
            <button
                onClick={onAccept}
                disabled={!canSubmit}
                className="w-full bg-brand text-white py-2 rounded hover:opacity-90 transition-opacity duration-200 disabled:opacity-40 disabled:cursor-not-allowed"
            >
                Accept & Continue
            </button>
        </Modal>
    );
}