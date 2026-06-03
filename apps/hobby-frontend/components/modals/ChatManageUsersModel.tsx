"use client";
import Image from "next/image";
import { Modal } from "./Modal";
import { ModalProps } from "@/types/interfaces";
import { User } from "@/types/profile";

interface ManageUsersModalProps extends ModalProps {
    chatUsersList: User[];
    chatId: string;
    followers: User[];
    fetchUsersAndFollowers: () => void;
    selfUser: User;
    deleteUser: (userId: string) => Promise<void>;
    addUser: (userId: string) => Promise<void>;
}

export function ChatManageUsersModal({ isOpen, onClose, chatUsersList, followers, fetchUsersAndFollowers, selfUser, deleteUser, addUser }: ManageUsersModalProps) {
    return (
        <Modal largeModal isOpen={isOpen} onClose={onClose}>
            <h2 className="text-lg font-semibold">Edit current users</h2>
            <div className="flex justify-between">
                <div className="rounded-xl border-brand border border-2 shadow-xl w-[40%] min-h-[40vh] max-h-[40vh] overflow-auto bg-brand/10 py-[1vh] px-[2vw] gap-[1vh] flex flex-col">
                    <p className="text-lg text-center">Existing users in chat</p>
                    {chatUsersList.map((user) =>
                        user.id !== selfUser.id && (
                            <div
                                key={user.id}
                                onClick={async () => {
                                    await deleteUser(user.id);
                                    fetchUsersAndFollowers();
                                    onClose();
                                }}
                                className="cursor-pointer border-brand border shadow-lg py-[1vh] px-[1vw] rounded-xl hover:border-red-500 hover:bg-red-400"
                            >
                                <div className="flex justify-between">
                                    <p>{user.username}</p>
                                    <Image src="/svgs/x.svg" alt="Remove user" width={25} height={25} />
                                </div>
                            </div>
                        ),
                    )}
                </div>
                <div className="rounded-xl border-brand border border-2 shadow-xl w-[40%] min-h-[40vh] max-h-[40vh] overflow-auto bg-brand/10 py-[1vh] px-[2vw] gap-[1vh] flex flex-col">
                    <p className="text-lg text-center">Followers</p>
                    {followers
                        .filter((follower) => !chatUsersList.some((user) => user.id === follower.id))
                        .map((follower) => (
                            <div
                                key={follower.id}
                                onClick={async () => {
                                    await addUser(follower.id);
                                    fetchUsersAndFollowers();
                                    onClose();
                                }} className="cursor-pointer border-brand border shadow-lg py-[1vh] px-[1vw] rounded-xl hover:border-red-500 hover:bg-red-400"
                            >
                                <div className="flex justify-between">
                                    <p>{follower.username}</p>
                                    <Image src="/svgs/plus.svg" alt="Add user" width={25} height={25} />
                                </div>
                            </div>
                        ))}
                </div>
            </div>
        </Modal>
    );
}