"use client";

import React, { useState } from "react";
import {
  Shield,
  ChevronRight,
  ChevronDown,
  Pencil,
  X,
  Check,
  Loader2,
} from "lucide-react";
import { useCurrentUser } from "@/hooks/useCurrentUser";
import { changePassword, updateEmail } from "@/lib/authenticationClient";


export function SecuritySettings() {
  const currentUser = useCurrentUser();
  const [isOpen, setIsOpen] = useState(false);

  // Email — seeded from the cookie-backed hook; falls back to empty string while loading
  const [email, setEmail] = useState("");
  const [emailInitialised, setEmailInitialised] = useState(false);
  const [editingEmail, setEditingEmail] = useState(false);
  const [emailDraft, setEmailDraft] = useState("");
  const [emailSaving, setEmailSaving] = useState(false);
  const [emailError, setEmailError] = useState("");
  const [emailSuccess, setEmailSuccess] = useState("");

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
  const [passwordLoading, setPasswordLoading] = useState(false);
  const [passwordError, setPasswordError] = useState("");
  const [passwordSuccess, setPasswordSuccess] = useState("");


  const cancelEmail = () => {
    setEditingEmail(false);
    setEmailDraft("");
    setEmailError("");
    setEmailSuccess("");
  };

  const saveEmail = async () => {
    setEmailError("");
    setEmailSuccess("");
    if (!emailDraft.trim()) {
      setEmailError("Email cannot be empty");
      return;
    }
    setEmailSaving(true);
    try {
      await updateEmail(emailDraft);
      setEmail(emailDraft);
      setEditingEmail(false);
      setEmailDraft("");
      setEmailSuccess("Email updated");
    } catch (err) {
      setEmailError(err instanceof Error ? err.message : "Failed to update email");
    } finally {
      setEmailSaving(false);
    }
  };

  const cancelPassword = () => {
    setChangingPassword(false);
    setCurrentPassword("");
    setNewPassword("");
    setConfirmPassword("");
    setPasswordError("");
    setPasswordSuccess("");
  };

  const savePassword = async () => {
    setPasswordError("");
    setPasswordSuccess("");
    if (!currentPassword || !newPassword || !confirmPassword) {
      setPasswordError("All fields are required");
      return;
    }
    if (newPassword !== confirmPassword) {
      setPasswordError("New passwords do not match");
      return;
    }
    if (newPassword.length < 8) {
      setPasswordError("New password must be at least 8 characters");
      return;
    }
    setPasswordLoading(true);
    try {
      await changePassword(currentPassword, newPassword);
      setPasswordSuccess("Password updated successfully");
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (err) {
      setPasswordError(
        err instanceof Error ? err.message : "Failed to change password"
      );
    } finally {
      setPasswordLoading(false);
    }
  };

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
          <p className="text-accent text-sm">Email and Password</p>
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
                {emailError && (
                  <p className="text-xs text-red-400">{emailError}</p>
                )}
                {emailSuccess && (
                  <p className="text-xs text-green-400">{emailSuccess}</p>
                )}
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
                    disabled={emailSaving}
                    className="flex items-center gap-1 text-xs text-white bg-brand hover:opacity-80 disabled:opacity-50 transition-opacity px-3 py-1.5 rounded-lg"
                  >
                    {emailSaving ? (
                      <Loader2 size={12} className="animate-spin" />
                    ) : (
                      <Check size={12} />
                    )}
                    {emailSaving ? "Saving..." : "Save"}
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
                {passwordError && (
                  <p className="text-xs text-red-400">{passwordError}</p>
                )}
                {passwordSuccess && (
                  <p className="text-xs text-green-400">{passwordSuccess}</p>
                )}
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
                  <button
                    onClick={savePassword}
                    disabled={passwordLoading}
                    className="flex items-center gap-1 text-xs text-white bg-brand hover:opacity-80 disabled:opacity-50 transition-opacity px-3 py-1.5 rounded-lg"
                  >
                    {passwordLoading ? (
                      <Loader2 size={12} className="animate-spin" />
                    ) : (
                      <Check size={12} />
                    )}
                    {passwordLoading ? "Saving..." : "Save"}
                  </button>
                </div>
              </div>
            ) : (
              <p className="text-sm text-white/80 min-h-6 tracking-widest">
                ••••••••
              </p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
