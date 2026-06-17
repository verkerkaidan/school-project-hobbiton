"use client";

import React, { useState, useEffect } from "react";
import {
  User,
  ChevronRight,
  ChevronDown,
  Pencil,
  X,
  Check,
  Loader2,
} from "lucide-react";
import { useCurrentUser } from "@/hooks/useCurrentUser";
import { getProfile } from "@/queries/getProfile";

interface ProfileField {
  key: "username" | "bio";
  label: string;
  placeholder: string;
  multiline?: boolean;
}

const PROFILE_FIELDS: ProfileField[] = [
  { key: "username", label: "Username", placeholder: "Your username" },
  {
    key: "bio",
    label: "Bio",
    placeholder: "Tell people a bit about yourself",
    multiline: true,
  },
];

type FieldValues = Record<ProfileField["key"], string>;

export function AccountSettings() {
  const currentUser = useCurrentUser();
  const [isOpen, setIsOpen] = useState(false);
  const [editing, setEditing] = useState<ProfileField["key"] | null>(null);
  const [values, setValues] = useState<FieldValues>({
    username: "",
    bio: "",
  });
  const [draft, setDraft] = useState("");
  const [loading, setLoading] = useState(false);
  const [fetched, setFetched] = useState(false);

  // Pre-populate username from the cached hook value immediately
  useEffect(() => {
    if (currentUser) {
      setValues((prev) => ({
        ...prev,
        username: prev.username || currentUser.username,
      }));
    }
  }, [currentUser]);

  const fetchProfile = async () => {
    if (fetched || !currentUser) return;
    setLoading(true);
    try {
      const profile = await getProfile(currentUser.id);
      setValues((prev) => ({
        ...prev,
        username: profile.username,
        bio: profile.bio ?? "",
      }));
      setFetched(true);
    } catch {
      // silently ignore — keep whatever is already populated
    } finally {
      setLoading(false);
    }
  };

  const handleToggle = () => {
    const next = !isOpen;
    setIsOpen(next);
    if (next) fetchProfile();
  };

  const startEdit = (key: ProfileField["key"]) => {
    setDraft(values[key]);
    setEditing(key);
  };

  const cancelEdit = () => {
    setEditing(null);
    setDraft("");
  };

  const saveEdit = (key: ProfileField["key"]) => {
    setValues((prev) => ({ ...prev, [key]: draft }));
    setEditing(null);
    setDraft("");
  };

  return (
    <div className="glass-card rounded-2xl overflow-hidden">
      <button
        onClick={handleToggle}
        className="w-full flex items-center gap-3 p-6 text-left hover:bg-white/2 transition-colors"
      >
        <div className="text-brand">
          <User size={20} />
        </div>
        <div className="flex-1">
          <h2 className="font-semibold text-base">Profile information</h2>
          <p className="text-accent text-sm">
            Update your public profile details
          </p>
        </div>
        {isOpen ? (
          <ChevronDown className="text-accent" size={18} />
        ) : (
          <ChevronRight className="text-accent" size={18} />
        )}
      </button>

      {isOpen && (
        <div className="border-t border-white/5 px-6 pb-6 pt-4 flex flex-col gap-5">
          {loading && (
            <div className="flex justify-center py-4">
              <Loader2 className="w-5 h-5 animate-spin text-brand" />
            </div>
          )}
          {!loading &&
            PROFILE_FIELDS.map(({ key, label, placeholder, multiline }) => (
              <div key={key}>
                <div className="flex items-center justify-between mb-1">
                  <span className="text-xs text-accent uppercase tracking-wide">
                    {label}
                  </span>
                  {editing !== key && (
                    <button
                      onClick={() => startEdit(key)}
                      className="flex items-center gap-1 text-xs text-brand hover:opacity-75 transition-opacity"
                    >
                      <Pencil size={12} />
                      Edit
                    </button>
                  )}
                </div>

                {editing === key ? (
                  <div className="flex flex-col gap-2">
                    {multiline ? (
                      <textarea
                        className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-sm text-white placeholder:text-accent focus:outline-none focus:border-brand resize-none"
                        rows={3}
                        value={draft}
                        placeholder={placeholder}
                        onChange={(e) => setDraft(e.target.value)}
                        autoFocus
                      />
                    ) : (
                      <input
                        type="text"
                        className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-sm text-white placeholder:text-accent focus:outline-none focus:border-brand"
                        value={draft}
                        placeholder={placeholder}
                        onChange={(e) => setDraft(e.target.value)}
                        autoFocus
                      />
                    )}
                    <div className="flex gap-2 justify-end">
                      <button
                        onClick={cancelEdit}
                        className="flex items-center gap-1 text-xs text-accent hover:text-white transition-colors px-3 py-1.5 rounded-lg border border-white/10 hover:border-white/20"
                      >
                        <X size={12} />
                        Cancel
                      </button>
                      <button
                        onClick={() => saveEdit(key)}
                        className="flex items-center gap-1 text-xs text-white bg-brand hover:opacity-80 transition-opacity px-3 py-1.5 rounded-lg"
                      >
                        <Check size={12} />
                        Save
                      </button>
                    </div>
                  </div>
                ) : (
                  <p className="text-sm text-white/80 min-h-6">
                    {values[key] || (
                      <span className="text-accent italic">Not set</span>
                    )}
                  </p>
                )}
              </div>
            ))}
        </div>
      )}
    </div>
  );
}
