"use client";

import React, { useState, useEffect } from "react";
import { Bell, ChevronRight, ChevronDown, Loader2 } from "lucide-react";
import {
  fetchNotificationPreferences,
  updateNotificationPreference,
} from "@/lib/notificationClient";
import {
  NotificationPreferences,
  NotificationType,
} from "@/types/notifications";

const TYPE_LABELS: Record<NotificationType, string> = {
  FOLLOW: "Follows",
  USER_POST: "Posts from followed users",
  RECOMMENDED_POST: "Recommended posts",
};

export function NotificationSettings() {
  const [isOpen, setIsOpen] = useState(false);
  const [prefs, setPrefs] = useState<NotificationPreferences | null>(null);
  const [loading, setLoading] = useState(false);
  const [fetched, setFetched] = useState(false);
  const [saving, setSaving] = useState<string | null>(null);

  const fetchPrefs = async () => {
    if (fetched) return;
    setLoading(true);
    try {
      const data = await fetchNotificationPreferences();
      setPrefs(data);
      setFetched(true);
    } catch {
      setPrefs(null);
    } finally {
      setLoading(false);
    }
  };

  const handleToggle = () => {
    const next = !isOpen;
    setIsOpen(next);
    if (next) fetchPrefs();
  };

  const handlePrefToggle = async (type: NotificationType, enabled: boolean) => {
    if (!prefs) return;
    setSaving(type);
    try {
      await updateNotificationPreference(type, enabled);
      setPrefs((prev) => (prev ? { ...prev, [type]: enabled } : prev));
    } catch {
      // silently ignore — toggle stays at previous value
    } finally {
      setSaving(null);
    }
  };

  return (
    <div className="glass-card rounded-2xl overflow-hidden">
      <button
        onClick={handleToggle}
        className="w-full flex items-center gap-3 p-6 text-left hover:bg-white/2 transition-colors"
      >
        <div className="text-brand">
          <Bell size={20} />
        </div>
        <div className="flex-1">
          <h2 className="font-semibold text-base">Notifications</h2>
          <p className="text-accent text-sm">Choose what you get notified about</p>
        </div>
        {isOpen ? (
          <ChevronDown className="text-accent" size={18} />
        ) : (
          <ChevronRight className="text-accent" size={18} />
        )}
      </button>

      {isOpen && (
        <div className="border-t border-white/5 px-6 pb-6 pt-4">
          {loading && (
            <div className="flex justify-center py-4">
              <Loader2 className="w-5 h-5 animate-spin text-brand" />
            </div>
          )}
          {!loading && !prefs && (
            <p className="text-sm text-accent">Could not load notification preferences.</p>
          )}
          {!loading && prefs && (
            <div className="flex flex-col gap-3 mt-1">
              {(Object.keys(TYPE_LABELS) as NotificationType[]).map((type) => (
                <div key={type} className="flex items-center justify-between">
                  <span className="text-sm text-accent">{TYPE_LABELS[type]}</span>
                  <button
                    onClick={() => handlePrefToggle(type, !prefs[type])}
                    disabled={saving === type}
                    aria-label={`Toggle ${TYPE_LABELS[type]}`}
                    className={`relative inline-flex h-6 w-11 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 focus:outline-none
                      ${prefs[type] ? "bg-brand" : "bg-white/20"}
                      ${saving === type ? "opacity-50 cursor-not-allowed" : ""}`}
                  >
                    <span
                      className={`pointer-events-none inline-block h-5 w-5 rounded-full bg-white shadow-lg transform transition duration-200
                        ${prefs[type] ? "translate-x-5" : "translate-x-0"}`}
                    />
                  </button>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}
