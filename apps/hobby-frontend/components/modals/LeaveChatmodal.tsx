import { Modal } from "./Modal";
import { useRouter } from "next/navigation";
import { LoginForm, ModalProps } from "@/types/interfaces";
import { User } from "@/types/profile";

interface ManageUsersModalProps extends ModalProps {
    user: User;
    deleteUser: (userId: string) => Promise<void>;
}
export function LeaveChatModal({ isOpen, onClose, deleteUser, user }: ManageUsersModalProps) {
    const router = useRouter();

    const handleSend = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await deleteUser(user.id);
        } finally {
            router.replace("/chats");
        }
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose}>
            <h2 className="text-xl font-bold mb-4">Leave Chat</h2>
            <p className="mb-6">Are you sure you want to leave this chat?</p>
                      <button
            onClick={handleSend}
            className="px-6 py-2.5 rounded-lg bg-brand text-white text-sm font-medium hover:opacity-90 transition-opacity duration-200"
          >
            Leave chat
          </button>
        </Modal>
    );
}
