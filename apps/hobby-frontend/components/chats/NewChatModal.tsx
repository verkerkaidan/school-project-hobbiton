"use client";

import { createChat } from "@/lib/chatClient";
import Cookies from "js-cookie";

import { useRouter } from "next/navigation";

import { useEffect, useState } from "react";

import { getFollowers } from "@/queries/getProfile";
import { User } from "@/types/profile";
export default function NewChatModal({
    open,
    onClose,
}: {
    open: boolean;
    onClose: () => void;
}) {
    const [selectedUsers, setSelectedUsers] =
        useState<string[]>([]);
    const [users, setUsers] = useState<User[]>([]);

    const router = useRouter();

    useEffect(() => {
        getFollowers(Cookies.get("userId")!).then(setUsers);
    }, []);
    const TEST_USERS = [
        { id: "00000000-0000-0000-0000-000000000001", name: "Alice" },
        { id: "00000000-0000-0000-0000-000000000002", name: "Bob" },
        { id: "00000000-0000-0000-0000-000000000003", name: "Charlie" },
        { id: "00000000-0000-0000-0000-000000000004", name: "Diana" },
        { id: "00000000-0000-0000-0000-000000000005", name: "Eve" },
        { id: "00000000-0000-0000-0000-000000000006", name: "Frank" },
    ];

    async function handleCreate() {
        const ownId = Cookies.get("userId");

        if (!ownId) return;

        const res = await createChat([
            ownId,
            ...selectedUsers,
        ]);

        router.push(`/chats/${res.chatId}`);
    }

    function toggle(user: string) {
        setSelectedUsers((prev) =>
            prev.includes(user)
                ? prev.filter((x) => x !== user)
                : [...prev, user]
        );
    }

    if (!open) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4">
            <div className="w-full max-w-xl rounded-3xl border border-zinc-800 bg-zinc-950 p-6">
                <div className="mb-6 flex items-center justify-between">
                    <h2 className="text-2xl font-bold">
                        New Chat
                    </h2>

                    <button
                        onClick={onClose}
                        className="rounded-xl bg-zinc-800 px-4 py-2"
                    >
                        Close
                    </button>
                </div>

                <div className="grid gap-3">
                    {users.map((user) => {
                        const selected = selectedUsers.includes(user.id);
                        return (
                            <button
                                key={user.id}
                                onClick={() => toggle(user.id)}
                                className={`rounded-2xl border p-4 text-left ${selected ? "border-blue-500 bg-blue-500/20" : "border-zinc-700 bg-zinc-900"
                                    }`}
                            >
                                {user.username}
                            </button>
                        );
                    })}
                </div>

                <button
                    onClick={handleCreate}
                    className="mt-6 w-full rounded-2xl bg-blue-500 px-5 py-4 font-semibold text-white"
                >
                    Start Chatting
                </button>
            </div>
        </div>
    );
}