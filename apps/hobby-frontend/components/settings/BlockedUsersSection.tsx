"use client";

import { useState, useCallback } from "react";
import Image from "next/image";
import Link from "next/link";
import { ChevronRight, ChevronDown, UserX, Loader2 } from "lucide-react";
import { useCurrentUser } from "@/hooks/useCurrentUser";
import { getBlockedUsers, toggleBlock } from "@/queries/getProfile";
import type { User } from "@/types/profile";

export function BlockedUsersSection() {
  const currentUser = useCurrentUser();
  const currentUserId = currentUser?.id ?? "";

  const [isOpen, setIsOpen] = useState(false);
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [unblockingIds, setUnblockingIds] = useState<Set<string>>(new Set());
  const [fetched, setFetched] = useState(false);

  const fetchBlocked = useCallback(async () => {
    if (fetched) return;
    setLoading(true);
    try {
      const blocked = await getBlockedUsers(currentUserId);
      setUsers(blocked);
      setFetched(true);
    } finally {
      setLoading(false);
    }
  }, [fetched, currentUserId]);

  const handleToggle = () => {
    const next = !isOpen;
    setIsOpen(next);
    if (next) fetchBlocked();
  };

  const handleUnblock = async (targetUserId: string) => {
    setUnblockingIds((prev) => new Set(prev).add(targetUserId));
    try {
      await toggleBlock(currentUserId, targetUserId);
      setUsers((prev) => prev.filter((u) => u.id !== targetUserId));
    } finally {
      setUnblockingIds((prev) => {
        const next = new Set(prev);
        next.delete(targetUserId);
        return next;
      });
    }
  };

  return (
    <div className="glass-card rounded-2xl overflow-hidden">
      {/* Header row — always visible */}
      <button
        onClick={handleToggle}
        className="w-full flex items-center gap-3 p-6 text-left hover:bg-white/2 transition-colors"
      >
        <div className="text-brand">
          <UserX size={20} />
        </div>
        <div className="flex-1">
          <h2 className="font-semibold text-base">Blocked Users</h2>
          <p className="text-accent text-sm">
            Manage the accounts you&apos;ve blocked
          </p>
        </div>
        {isOpen ? (
          <ChevronDown className="text-accent" size={18} />
        ) : (
          <ChevronRight className="text-accent" size={18} />
        )}
      </button>

      {/* Expandable list */}
      {isOpen && (
        <div className="border-t border-white/5 px-6 pb-6 pt-4">
          {loading && (
            <div className="flex items-center justify-center py-8 text-accent gap-2">
              <Loader2 size={16} className="animate-spin" />
              <span className="text-sm">Loading…</span>
            </div>
          )}

          {!loading && users.length === 0 && (
            <p className="text-accent text-sm text-center py-6">
              You haven&apos;t blocked anyone.
            </p>
          )}

          {!loading && users.length > 0 && (
            <ul className="flex flex-col gap-3">
              {users.map((user) => (
                <li
                  key={user.id}
                  className="flex items-center gap-3 rounded-xl px-3 py-2 hover:bg-white/3 transition-colors"
                >
                  <Link
                    href={`/profile/${user.id}`}
                    className="flex items-center gap-3 flex-1 min-w-0"
                  >
                    <Image
                      src={user.profilePictureUrl || "/jpeg.png"}
                      alt={user.username}
                      width={36}
                      height={36}
                      className="rounded-full object-cover shrink-0"
                    />
                    <span className="truncate text-sm font-medium">
                      {user.username}
                    </span>
                  </Link>
                  <button
                    onClick={() => handleUnblock(user.id)}
                    disabled={unblockingIds.has(user.id)}
                    className="shrink-0 text-xs font-medium px-3 py-1.5 rounded-lg border border-white/10 text-accent hover:text-white hover:border-white/25 transition-colors disabled:opacity-40 disabled:cursor-not-allowed flex items-center gap-1.5"
                  >
                    {unblockingIds.has(user.id) && (
                      <Loader2 size={12} className="animate-spin" />
                    )}
                    Unblock
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  );
}
