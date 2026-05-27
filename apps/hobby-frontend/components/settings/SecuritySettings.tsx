"use client";

import React, { useState } from "react";
import {
  Shield,
  ChevronRight,
  ChevronDown,
  Pencil,
  X,
  Check,
  Monitor,
  Smartphone,
  Globe,
} from "lucide-react";
import { useCurrentUser } from "@/hooks/useCurrentUser";

// ── mock data ─────────────────────────────────────────────────────────────────

interface MockSession {
  id: string;
  device: string;
  location: string;
  lastSeen: string;
  isCurrent: boolean;
  icon: "desktop" | "mobile" | "web";
}

const MOCK_SESSIONS: MockSession[] = [
  {
    id: "1",
    device: "Chrome on macOS",
    location: "Amsterdam, NL",
    lastSeen: "Now",
    isCurrent: true,
    icon: "desktop",
  },
  {
    id: "2",
    device: "Safari on iPhone",
    location: "Amsterdam, NL",
    lastSeen: "2 hours ago",
    isCurrent: false,
    icon: "mobile",
  },
  {
    id: "3",
    device: "Firefox on Windows",
    location: "Rotterdam, NL",
    lastSeen: "3 days ago",
    isCurrent: false,
    icon: "web",
  },
];

// ─────────────────────────────────────────────────────────────────────────────

function SessionIcon({ type }: { type: MockSession["icon"] }) {
  if (type === "mobile")
    return <Smartphone size={14} className="text-accent" />;
  if (type === "web") return <Globe size={14} className="text-accent" />;
  return <Monitor size={14} className="text-accent" />;
}

export function SecuritySettings() {
  const currentUser = useCurrentUser();
  const [isOpen, setIsOpen] = useState(false);

  // Email — seeded from the cookie-backed hook; falls back to empty string while loading
  const [email, setEmail] = useState("");
  const [emailInitialised, setEmailInitialised] = useState(false);
  const [editingEmail, setEditingEmail] = useState(false);
  const [emailDraft, setEmailDraft] = useState("");

  // Seed the email state once the hook resolves
  React.useEffect(() => {
    if (!emailInitialised && currentUser?.email) {
      setEmail(currentUser.email);
      setEmailInitialised(true);
    }
  }, [currentUser, emailInitialised]);

  // Password
  const [changingPassword, setChangingPassword] = useState(false);
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  // 2FA (mocked off)
  const [twoFaEnabled, setTwoFaEnabled] = useState(false);

  // Sessions
  const [sessions, setSessions] = useState<MockSession[]>(MOCK_SESSIONS);

  const cancelEmail = () => {
    setEditingEmail(false);
    setEmailDraft("");
  };

  const saveEmail = () => {
    setEmail(emailDraft);
    setEditingEmail(false);
    setEmailDraft("");
  };

  const cancelPassword = () => {
    setChangingPassword(false);
    setCurrentPassword("");
    setNewPassword("");
    setConfirmPassword("");
  };

  const revokeSession = (id: string) =>
    setSessions((prev) => prev.filter((s) => s.id !== id));

  const revokeAll = () =>
    setSessions((prev) => prev.filter((s) => s.isCurrent));

  return (
    <div className="glass-card rounded-2xl overflow-hidden">
      {/* ── Header ────────────────────────────────────────────────────────── */}
      <button
        onClick={() => setIsOpen((prev) => !prev)}
        className="w-full flex items-center gap-3 p-6 text-left hover:bg-white/2 transition-colors"
      >
        <div className="text-brand">
          <Shield size={20} />
        </div>
        <div className="flex-1">
          <h2 className="font-semibold text-base">Security</h2>
          <p className="text-accent text-sm">
            Password, two-factor authentication, and sessions
          </p>
        </div>
        {isOpen ? (
          <ChevronDown className="text-accent" size={18} />
        ) : (
          <ChevronRight className="text-accent" size={18} />
        )}
      </button>

      {isOpen && (
        <div className="border-t border-white/5 divide-y divide-white/5">
          {/* ── Email ───────────────────────────────────────────────────── */}
          <div className="px-6 py-5">
            <div className="flex items-center justify-between mb-1">
              <span className="text-xs text-accent uppercase tracking-wide">
                Email address
              </span>
              {!editingEmail && (
                <button
                  onClick={() => {
                    setEmailDraft(email);
                    setEditingEmail(true);
                  }}
                  className="flex items-center gap-1 text-xs text-brand hover:opacity-75 transition-opacity"
                >
                  <Pencil size={12} />
                  Edit
                </button>
              )}
            </div>

            {editingEmail ? (
              <div className="flex flex-col gap-2 mt-1">
                <input
                  type="email"
                  className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-sm text-white placeholder:text-accent focus:outline-none focus:border-brand"
                  value={emailDraft}
                  placeholder="New email address"
                  onChange={(e) => setEmailDraft(e.target.value)}
                  autoFocus
                />
                <div className="flex gap-2 justify-end">
                  <button
                    onClick={cancelEmail}
                    className="flex items-center gap-1 text-xs text-accent hover:text-white transition-colors px-3 py-1.5 rounded-lg border border-white/10 hover:border-white/20"
                  >
                    <X size={12} />
                    Cancel
                  </button>
                  <button
                    onClick={saveEmail}
                    className="flex items-center gap-1 text-xs text-white bg-brand hover:opacity-80 transition-opacity px-3 py-1.5 rounded-lg"
                  >
                    <Check size={12} />
                    Save
                  </button>
                </div>
              </div>
            ) : (
              <p className="text-sm text-white/80 min-h-6">{email}</p>
            )}
          </div>

          {/* ── Password ────────────────────────────────────────────────── */}
          <div className="px-6 py-5">
            <div className="flex items-center justify-between mb-1">
              <span className="text-xs text-accent uppercase tracking-wide">
                Password
              </span>
              {!changingPassword && (
                <button
                  onClick={() => setChangingPassword(true)}
                  className="flex items-center gap-1 text-xs text-brand hover:opacity-75 transition-opacity"
                >
                  <Pencil size={12} />
                  Change
                </button>
              )}
            </div>

            {changingPassword ? (
              <div className="flex flex-col gap-2 mt-1">
                <input
                  type="password"
                  className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-sm text-white placeholder:text-accent focus:outline-none focus:border-brand"
                  value={currentPassword}
                  placeholder="Current password"
                  onChange={(e) => setCurrentPassword(e.target.value)}
                  autoFocus
                />
                <input
                  type="password"
                  className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-sm text-white placeholder:text-accent focus:outline-none focus:border-brand"
                  value={newPassword}
                  placeholder="New password"
                  onChange={(e) => setNewPassword(e.target.value)}
                />
                <input
                  type="password"
                  className="w-full bg-white/5 border border-white/10 rounded-lg px-3 py-2 text-sm text-white placeholder:text-accent focus:outline-none focus:border-brand"
                  value={confirmPassword}
                  placeholder="Confirm new password"
                  onChange={(e) => setConfirmPassword(e.target.value)}
                />
                <div className="flex gap-2 justify-end">
                  <button
                    onClick={cancelPassword}
                    className="flex items-center gap-1 text-xs text-accent hover:text-white transition-colors px-3 py-1.5 rounded-lg border border-white/10 hover:border-white/20"
                  >
                    <X size={12} />
                    Cancel
                  </button>
                  <button className="flex items-center gap-1 text-xs text-white bg-brand hover:opacity-80 transition-opacity px-3 py-1.5 rounded-lg">
                    <Check size={12} />
                    Save
                  </button>
                </div>
              </div>
            ) : (
              <p className="text-sm text-white/80 min-h-6 tracking-widest">
                ••••••••
              </p>
            )}
          </div>

          {/* ── Two-factor authentication ────────────────────────────────── */}
          <div className="px-6 py-5">
            <div className="flex items-center justify-between">
              <div>
                <span className="text-xs text-accent uppercase tracking-wide">
                  Two-factor authentication
                </span>
                <p className="text-sm text-white/80 mt-1">
                  {twoFaEnabled ? "Enabled" : "Disabled"}
                </p>
              </div>
              <button
                onClick={() => setTwoFaEnabled((prev) => !prev)}
                aria-label="Toggle two-factor authentication"
                className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 focus:outline-none
                  ${twoFaEnabled ? "bg-brand" : "bg-white/20"}`}
              >
                <span
                  className={`pointer-events-none inline-block h-5 w-5 rounded-full bg-white shadow-lg transform transition duration-200
                    ${twoFaEnabled ? "translate-x-5" : "translate-x-0"}`}
                />
              </button>
            </div>
          </div>

          {/* ── Active sessions (Out of scope for now) ──────────────────────────────────────────── */}
          {/* <div className="px-6 py-5">
            <div className="flex items-center justify-between mb-3">
              <span className="text-xs text-accent uppercase tracking-wide">
                Active sessions
              </span>
              {sessions.some((s) => !s.isCurrent) && (
                <button
                  onClick={revokeAll}
                  className="text-xs text-red-400 hover:opacity-75 transition-opacity"
                >
                  Sign out all others
                </button>
              )}
            </div>
            <div className="flex flex-col gap-2">
              {sessions.map((session) => (
                <div
                  key={session.id}
                  className="flex items-center gap-3 py-2 px-3 rounded-lg bg-white/3"
                >
                  <SessionIcon type={session.icon} />
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-white/80 truncate">
                      {session.device}
                    </p>
                    <p className="text-xs text-accent">
                      {session.location} · {session.lastSeen}
                    </p>
                  </div>
                  {session.isCurrent ? (
                    <span className="text-xs text-brand">Current</span>
                  ) : (
                    <button
                      onClick={() => revokeSession(session.id)}
                      className="text-xs text-red-400 hover:opacity-75 transition-opacity"
                    >
                      Sign out
                    </button>
                  )}
                </div>
              ))}
            </div>
          </div> */}
        </div>
      )}
    </div>
  );
}
