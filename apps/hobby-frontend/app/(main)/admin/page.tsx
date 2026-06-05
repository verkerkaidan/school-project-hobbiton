"use client";

import React, { useEffect } from "react";
import { useRouter } from "next/navigation";
import { ShieldAlert } from "lucide-react";
import { useCurrentUser } from "@/hooks/useCurrentUser";
import { UsersOverview } from "@/components/admin/UsersOverview";
import { ReportsTable } from "@/components/admin/ReportsTable";

export default function AdminReportsPage() {
  const currentUser = useCurrentUser();
  const router = useRouter();

  const isAdmin = currentUser?.isAdmin ?? false;

  useEffect(() => {
    // Once we have a resolved user and they're not admin, redirect.
    if (currentUser !== null && !isAdmin) {
      router.replace("/");
    }
  }, [currentUser, isAdmin, router]);

  // While the user hasn't resolved yet, render nothing to avoid flicker.
  if (!currentUser || !isAdmin) return null;

  return (
    <div className="mx-auto px-6 pt-14 pb-20 max-w-7xl">
      {/* Header */}
      <header className="mb-10">
        <div className="flex items-center gap-3 mb-2">
          <div className="w-9 h-9 rounded-xl bg-rose-500/10 border border-rose-500/20 flex items-center justify-center">
            <ShieldAlert className="w-5 h-5 text-rose-400" />
          </div>
          <div>
            <h1 className="text-xl font-bold text-accent">Admin</h1>
            <p className="text-accent/60 text-xs">
              Admin panel · Users and reports
            </p>
          </div>
        </div>
        <p className="text-accent/70 text-sm mt-4 max-w-xl">
          View all user profiles and moderate reports submitted by users.
        </p>
      </header>

      {/* Users overview */}
      <UsersOverview />

      {/* Reports table */}
      <div className="mt-8">
        <ReportsTable />
      </div>
    </div>
  );
}
