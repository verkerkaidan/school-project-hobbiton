"use client";

import React, { useEffect, useState } from "react";
import Link from "next/link";
import { RefreshCw, Users } from "lucide-react";
import {
  getAllAdminProfiles,
  type AdminProfileSummary,
} from "@/lib/adminClient";

const DEFAULT_PROFILE_IMAGE = "/jpeg.png";

function UserImage({ profile }: { profile: AdminProfileSummary }) {
  const [imageFailed, setImageFailed] = useState(false);

  const imageSrc = profile.profilePicture?.fileData
          ? `data:${profile.profilePicture.contentType};base64,${profile.profilePicture.fileData}`
          : DEFAULT_PROFILE_IMAGE;

  return (
    <img
      src={imageSrc}
      alt={profile.username}
      className="h-14 w-14 rounded-full object-cover border border-accent/20"
      onError={() => {
        if (!imageFailed) setImageFailed(true);
      }}
      loading="lazy"
    />
  );
}

export function UsersOverview() {
  const [profiles, setProfiles] = useState<AdminProfileSummary[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState("");

  const normalizedQuery = searchQuery.trim().toLowerCase();
  const filteredProfiles = profiles.filter((profile) =>
    profile.username.toLowerCase().includes(normalizedQuery),
  );

  const fetchProfiles = async () => {
    setLoading(true);
    setError(null);
    try {
      const allProfiles = await getAllAdminProfiles();
      setProfiles(allProfiles);
    } catch {
      setError("Failed to load users.");
      setProfiles([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfiles();
  }, []);

  return (
    <section className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Users className="w-4 h-4 text-accent/80" />
          <h2 className="text-sm font-semibold text-accent">All Users</h2>
          <span className="text-xs text-accent/60">
            {filteredProfiles.length}
          </span>
        </div>

        <button
          onClick={fetchProfiles}
          disabled={loading}
          className="w-8 h-8 rounded-lg border border-accent/20 bg-background-secondary text-accent/70 hover:text-accent hover:border-accent/30 transition-colors disabled:opacity-40 flex items-center justify-center"
          title="Refresh users"
        >
          <RefreshCw
            className={`w-3.5 h-3.5 ${loading ? "animate-spin" : ""}`}
          />
        </button>
      </div>

      <div>
        <input
          type="text"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          placeholder="Search by username"
          className="w-full sm:max-w-md rounded-xl border border-accent/20 bg-background-secondary px-4 py-2.5 text-sm text-accent placeholder:text-accent/50 focus:outline-none focus:ring-2 focus:ring-brand/20"
        />
      </div>

      <div className="rounded-2xl border border-accent/15 bg-background-secondary p-4 sm:p-5">
        {error && (
          <div className="px-5 py-4 text-sm text-rose-400 bg-rose-500/10 border-t border-rose-500/20">
            {error}
          </div>
        )}

        {!error && !loading && filteredProfiles.length === 0 && (
          <div className="px-5 py-8 text-sm text-accent/70">
            {profiles.length === 0
              ? "No users found."
              : "No users match your search."}
          </div>
        )}

        {loading && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            {[...Array(4)].map((_, i) => (
              <div
                key={i}
                className="rounded-xl border border-accent/15 bg-background p-4 animate-pulse"
              >
                <div className="h-14 w-14 rounded-full bg-accent/10 mb-3" />
                <div className="h-4 w-32 rounded bg-accent/10" />
              </div>
            ))}
          </div>
        )}

        {!error && !loading && filteredProfiles.length > 0 && (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
            {filteredProfiles.map((profile) => (
              <Link
                key={profile.username}
                href={`/profile/${encodeURIComponent(profile.username)}`}
                className="rounded-xl border border-accent/15 bg-background p-4 hover:bg-background-secondary hover:border-accent/30 transition-colors"
              >
                <div className="flex items-center gap-3">
                  <UserImage profile={profile} />
                  <span className="text-base text-accent font-medium break-all">
                    {profile.username}
                  </span>
                </div>
              </Link>
            ))}
          </div>
        )}
      </div>
    </section>
  );
}
