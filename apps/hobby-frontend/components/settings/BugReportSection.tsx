"use client";

import React, { useState } from "react";
import { Bug } from "lucide-react";
import { BugReportModal } from "@/components/modals/BugReportModal";

export function BugReportSection() {
  const [showModal, setShowModal] = useState(false);

  return (
    <>
      <div className="glass-card rounded-2xl overflow-hidden">
        <button
          onClick={() => setShowModal(true)}
          className="w-full flex items-center gap-3 p-6 text-left hover:bg-white/2 transition-colors"
        >
          <div className="text-accent">
            <Bug size={20} />
          </div>
          <div className="flex-1">
            <h2 className="font-semibold text-base">Report a bug</h2>
            <p className="text-accent text-sm">
              Found an issue? Let us know so we can fix it
            </p>
          </div>
        </button>
      </div>

      <BugReportModal isOpen={showModal} onClose={() => setShowModal(false)} />
    </>
  );
}
