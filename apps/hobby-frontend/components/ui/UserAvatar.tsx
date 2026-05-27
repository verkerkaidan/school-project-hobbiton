"use client";
import { motion } from "framer-motion";
import Image from "next/image";
import Link from "next/link";
import Cookies from "js-cookie";
import { useRouter } from "next/navigation";
import { clearCurrentUser } from "@/hooks/useCurrentUser";

interface UserAvatarProps {
  name: string;
  role: string;
  imageUrl: string;
  isOnline?: boolean;
}

export const UserAvatar = ({
  name,
  role,
  imageUrl,
  isOnline = true,
}: UserAvatarProps) => {
  const router = useRouter();

  const handleLogout = () => {
    Cookies.remove("token");
    Cookies.remove("userId");
    Cookies.remove("email");
    clearCurrentUser();
    router.push("/authentication");
  };

  return (
    <div className="mt-auto w-full pt-6 border-t border-white/5 flex flex-col md:flex-row items-center gap-3">
      <Link
        href="/profile"
        className="flex items-center gap-3 min-w-0 group/avatar"
      >
        <div className="relative w-10 h-10 md:w-12 md:h-12 rounded-full overflow-hidden border-2 border-brand/20 shrink-0 group-hover/avatar:border-brand/60 transition-colors">
          <Image src={imageUrl} alt={name} fill className={"object-cover"} />
          {isOnline && (
            <div className="absolute bottom-0 right-0 w-3 h-3 bg-emerald-500 rounded-full border-2 border-background" />
          )}
        </div>

        <div className="hidden md:block overflow-hidden">
          <p className="text-sm font-semibold truncate group-hover/avatar:text-brand-light transition-colors">
            {name}
          </p>
          <p className="text-xs text-accent truncate">{role}</p>
        </div>
      </Link>

      <motion.button
        whileHover={{ scale: 1.1, x: 2 }}
        whileTap={{ scale: 0.9 }}
        onClick={() => handleLogout()}
        className="ml-auto p-2 text-accent hover:text-brand-light transition-colors group relative"
      >
        <svg
          xmlns="http://www.w3.org/2000/svg"
          width="20"
          height="20"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
          className="group-hover:drop-shadow-[0_0_8px_rgba(244,114,182,0.4)]"
        >
          <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
          <polyline points="16 17 21 12 16 7" />
          <line x1="21" x2="9" y1="12" y2="12" />
        </svg>
      </motion.button>
    </div>
  );
};
