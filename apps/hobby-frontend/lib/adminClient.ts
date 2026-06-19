import Cookies from "js-cookie";
import type { Report, ReportStatus } from "@/types/reports";
import type { MediaSet } from "@/types/posts";

export interface ProfileSummary {
  userId?: string
  username: string;
  profilePicture?: MediaSet | null;
  profilePictureUrl?: string | null;
}

/** Base URL for the API gateway — same origin used by the rest of the app. */
const MIDDLEWARE_URL = process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE ?? "";

function authHeaders(extra?: Record<string, string>): Record<string, string> {
  const token = Cookies.get("token") ?? "";
  return { Authorization: `Bearer ${token}`, ...extra };
}

/**
 * Fetch reports filtered by status.
 * Pass "ALL" to retrieve every report regardless of status — the gateway
 * handles this in a single call to the profile-connection-service and enriches
 * each report with initiatorUsername / targetUsername before responding.
 */
export async function getReportsByStatus(
  status: ReportStatus | "ALL",
): Promise<Report[]> {
  const res = await fetch(
    `${MIDDLEWARE_URL}/api/admin/reports/status/${status}`,
    { headers: authHeaders() },
  );
  if (!res.ok) throw new Error("Failed to fetch reports");
  return res.json();
}

/** Update the status of a single report. */
export async function updateReportStatus(
  reportId: string,
  status: ReportStatus,
): Promise<void> {
  const res = await fetch(
    `${MIDDLEWARE_URL}/api/admin/reports/${reportId}/status`,
    {
      method: "PUT",
      headers: authHeaders({ "Content-Type": "application/json" }),
      body: JSON.stringify({ status }),
    },
  );
  if (!res.ok) throw new Error("Failed to update report status");
}

/** Hard-delete a single post by its ID. */
export async function deletePost(postId: string): Promise<void> {
  const res = await fetch(`${MIDDLEWARE_URL}/api/admin/posts/${postId}`, {
    method: "DELETE",
    headers: authHeaders(),
  });
  if (!res.ok) throw new Error("Failed to delete post");
}

/** Hard-delete every post belonging to a given user. */
export async function deleteAllUserPosts(userId: string): Promise<void> {
  const res = await fetch(`${MIDDLEWARE_URL}/api/admin/posts/users/${userId}`, {
    method: "DELETE",
    headers: authHeaders(),
  });
  if (!res.ok) throw new Error("Failed to delete all user posts");
}

/** Fetch all user profiles (username + profile picture only) in a single call. */
export async function getAllAdminProfiles(): Promise<ProfileSummary[]> {
  const res = await fetch(`${MIDDLEWARE_URL}/api/admin/profiles`, {
    headers: authHeaders(),
  });
  if (!res.ok) throw new Error("Failed to fetch profiles");
  return res.json();
}
