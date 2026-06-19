import Cookies from "js-cookie";

const MIDDLEWARE_URL = process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE ?? "";

export interface BugReport {
  id: string;
  title: string;
  description: string;
  status: "OPEN" | "CLOSED";
  createdAt: string;
  closedAt: string | null;
}

/** Submit a new bug report. Reads the stored auth token from localStorage. */
export async function createBugReport(
  title: string,
  description: string,
): Promise<void> {
  const token = Cookies.get("token");
  const res = await fetch(`${MIDDLEWARE_URL}/api/bug-reports`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ title, description }),
  });
  if (!res.ok) throw new Error("Failed to submit bug report");
}

/** Fetch all bug reports from the admin endpoint.
 *  Pass status="OPEN" or "CLOSED" to filter, or omit for all reports. */
export async function getAllBugReports(
  status?: "OPEN" | "CLOSED",
): Promise<BugReport[]> {
  const token = Cookies.get("token") ?? "";
  const url = new URL(`${MIDDLEWARE_URL}/api/admin/bug-reports`);
  if (status) url.searchParams.set("status", status);
  const res = await fetch(url.toString(), {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!res.ok) throw new Error("Failed to fetch bug reports");
  return res.json();
}

/** Close a bug report by ID. */
export async function closeBugReport(id: string): Promise<BugReport> {
  const token = Cookies.get("token") ?? "";
  const res = await fetch(
    `${MIDDLEWARE_URL}/api/admin/bug-reports/${id}/close`,
    {
      method: "PUT",
      headers: { Authorization: `Bearer ${token}` },
    },
  );
  if (!res.ok) throw new Error("Failed to close bug report");
  return res.json();
}
