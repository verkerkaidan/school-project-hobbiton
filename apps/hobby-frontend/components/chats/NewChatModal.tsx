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
    const [title, setTitle] = useState<string>("");
    const router = useRouter();
    const [selectedUsersError, setSelectedUsersError] = useState(false);
    const [titleError, setTitleError] = useState(false);

    useEffect(() => {
        getFollowers(Cookies.get("userId")!).then(setUsers);
    }, []);

    async function handleCreate() {

        if (title.length < 5 || title.length > 50) {
            setTitleError(true);
            return;
        }
        if (selectedUsers.length == 0) {
            setSelectedUsersError(true);
            return;
        }

        const ownId = Cookies.get("userId");

        if (!ownId) return;

        const res = await createChat({
            "userIds": [
                ownId,
                ...selectedUsers,
            ],
            "title": title
        });

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
            <div className="w-full max-w-xl rounded-3xl border border-zinc-800 bg-zinc-950 p-6 gap-5 flex flex-col">
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
                <div className="flex flex-col gap-1">
                    <label
                        htmlFor="create-chat-title"
                        className="text-sm font-medium text-white"
                    >
                        Chat title
                    </label>
                    <p className="text-xs font-small text-brand">Required</p>
                    <input
                        id="create-chat-title"
                        name="title"
                        type="text"
                        placeholder=""
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        className={`rounded-xl px-3 py-2 text-sm focus:border-brand focus:border focus:ring-2 focus:ring-brand w-full bg-zinc-900`}
                    />
                    {titleError && (
                        <p className="text-red-500 font-bold italic text-xs">Title of chat is required</p>
                    )}

                </div>
                <div className="flex flex-col gap-1">
                    <label
                        htmlFor="select-users-title"
                        className="text-sm font-medium text-white"
                    >
                        Select users
                    </label>
                    <p className="text-xs font-small text-brand">Required</p>
                    <div className="grid gap-3">
                        {users.map((user) => {
                            const selected = selectedUsers.includes(user.id);
                            return (
                                <button
                                    key={user.id}
                                    onClick={() => toggle(user.id)}
                                    className={`rounded-2xl border p-4 text-left ${selected ? "border-brand bg-brand/20" : "border-zinc-700 bg-zinc-900"
                                        }`}
                                >
                                    {user.username}
                                </button>
                            );
                        })}
                    </div>
                    {selectedUsersError && (
                        <p className="text-red-500 font-bold italic text-xs">At least 1 other user is required</p>
                    )}

                </div>
                <button
                    onClick={handleCreate}
                    className="mt-6 w-full rounded-2xl  hover:bg-brand/80 px-5 bg-brand py-4 font-semibold text-white"
                >
                    Start Chatting
                </button>
            </div>
        </div>
    );
}