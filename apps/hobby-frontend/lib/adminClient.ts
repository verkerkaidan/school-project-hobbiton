import type { Report, ReportStatus } from "@/types/reports";
import type { MediaSet } from "@/types/posts";

/** Base URL for the API gateway — same origin used by the rest of the app. */
const MIDDLEWARE_URL = process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE ?? "";

/** Fetch reports filtered by status. Pass "ALL" to fetch every status in parallel. */
export async function getReportsByStatus(
  status: ReportStatus | "ALL",
): Promise<Report[]> {
  if (status === "ALL") {
    const statuses: ReportStatus[] = [
      "PENDING",
      "UNDER_REVIEW",
      "RESOLVED",
      "DISMISSED",
      "ACTION_TAKEN",
    ];
    const results = await Promise.all(
      statuses.map((s) =>
        fetch(`${MIDDLEWARE_URL}/api/admin/reports/status/${s}`)
          .then((r) => (r.ok ? (r.json() as Promise<Report[]>) : []))
          .catch(() => [] as Report[]),
      ),
    );
    return results.flat();
  }

  const res = await fetch(
    `${MIDDLEWARE_URL}/api/admin/reports/status/${status}`,
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
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ status }),
    },
  );
  if (!res.ok) throw new Error("Failed to update report status");
}

/** Hard-delete a single post by its ID. */
export async function deletePost(postId: string): Promise<void> {
  const res = await fetch(`${MIDDLEWARE_URL}/api/admin/posts/${postId}`, {
    method: "DELETE",
  });
  if (!res.ok) throw new Error("Failed to delete post");
}

/** Hard-delete every post belonging to a given user. */
export async function deleteAllUserPosts(userId: string): Promise<void> {
  const res = await fetch(`${MIDDLEWARE_URL}/api/admin/posts/users/${userId}`, {
    method: "DELETE",
  });
  if (!res.ok) throw new Error("Failed to delete all user posts");
}

