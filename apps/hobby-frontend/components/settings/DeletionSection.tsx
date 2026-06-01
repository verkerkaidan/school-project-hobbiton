"use client";

import React, { useState } from "react";
import {
  Trash2,
  ChevronRight,
  ChevronDown,
  TriangleAlert,
  X,
} from "lucide-react";
import { useCurrentUser } from "@/hooks/useCurrentUser";

function ConfirmationModal({
  username,
  onClose,
}: {
  username: string;
  onClose: () => void;
}) {
  const [typed, setTyped] = useState("");
  const confirmed = typed === username;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
      {/* Backdrop */}
      <div
        className="absolute inset-0 bg-black/60 backdrop-blur-sm"
        onClick={onClose}
      />

      {/* Dialog */}
      <div className="relative glass-card rounded-2xl p-6 w-full max-w-md flex flex-col gap-5">
        {/* Close */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-accent hover:text-white transition-colors"
          aria-label="Close"
        >
          <X size={18} />
        </button>

        {/* Warning icon */}
        <div className="flex flex-col items-center gap-3 text-center">
          <div className="flex items-center justify-center w-14 h-14 rounded-full bg-red-500/10 text-red-400">
            <TriangleAlert size={28} />
          </div>
          <div>
            <h2 className="text-lg font-bold text-white">
              Delete your account?
            </h2>
            <p className="text-sm text-accent mt-1 leading-relaxed">
              This will permanently delete your profile, posts, and all
              associated data.{" "}
              <span className="text-red-400 font-medium">
                This cannot be undone.
              </span>
            </p>
          </div>
        </div>

        {/* Consequences */}
        <ul className="text-xs text-accent space-y-1.5 bg-white/3 rounded-xl px-4 py-3">
          {[
            "Your profile and username will be gone forever",
            "All your posts and media will be deleted",
            "Your followers and following lists will be erased",
          ].map((line) => (
            <li key={line} className="flex items-start gap-2">
              <span className="text-red-400 mt-px shrink-0">✕</span>
              {line}
            </li>
          ))}
        </ul>

        {/* Confirmation input */}
        <div className="flex flex-col gap-2">
          <label className="text-xs text-accent">
            Type{" "}
            <span className="text-white font-mono font-medium">{username}</span>{" "}
            to confirm
          </label>
          <input
            type="text"
            className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-sm text-white placeholder:text-accent focus:outline-none focus:border-red-500/60"
            placeholder={username}
            value={typed}
            onChange={(e) => setTyped(e.target.value)}
            autoFocus
          />
        </div>

        {/* Actions */}
        <div className="flex gap-2 justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 text-xs text-accent hover:text-white transition-colors rounded-lg border border-white/10 hover:border-white/20"
          >
            Cancel
          </button>
          <button
            disabled={!confirmed}
            className="px-4 py-2 text-xs font-medium rounded-lg transition-opacity
              bg-red-500 text-white
              disabled:opacity-30 disabled:cursor-not-allowed
              enabled:hover:opacity-80"
          >
            Delete account
          </button>
        </div>
      </div>
    </div>
  );
}

export function DeletionSection() {
  const currentUser = useCurrentUser();
  const [isOpen, setIsOpen] = useState(false);
  const [showModal, setShowModal] = useState(false);

  const username = currentUser?.username ?? "your account";

  return (
    <>
      <div className="glass-card rounded-2xl overflow-hidden border border-red-500/10">
        <button
          onClick={() => setIsOpen((prev) => !prev)}
          className="w-full flex items-center gap-3 p-6 text-left hover:bg-white/2 transition-colors"
        >
          <div className="text-red-400">
            <Trash2 size={20} />
          </div>
          <div className="flex-1">
            <h2 className="font-semibold text-base text-red-400">
              Delete account
            </h2>
            <p className="text-accent text-sm">
              Permanently remove your account and all its data
            </p>
          </div>
          {isOpen ? (
            <ChevronDown className="text-accent" size={18} />
          ) : (
            <ChevronRight className="text-accent" size={18} />
          )}
        </button>

        {isOpen && (
          <div className="border-t border-red-500/10 px-6 py-5 flex flex-col gap-4">
            <p className="text-sm text-accent leading-relaxed">
              Once you delete your account, there is no going back. All your
              data — including posts, followers, and profile information — will
              be permanently erased.
            </p>
            <div className="flex justify-end">
              <button
                onClick={() => setShowModal(true)}
                className="px-4 py-2 text-xs font-medium rounded-lg bg-red-500/10 text-red-400 border border-red-500/20 hover:bg-red-500/20 transition-colors"
              >
                Delete my account
              </button>
            </div>
          </div>
        )}
      </div>

      {showModal && (
        <ConfirmationModal
          username={username}
          onClose={() => setShowModal(false)}
        />
      )}
    </>
  );
}
