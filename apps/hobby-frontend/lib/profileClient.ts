import Cookies from "js-cookie";
import {authHeaders} from "@/lib/adminClient";

const GATEWAY_URL =
  process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE ?? "http://localhost:23738";

const API_BASE = `${GATEWAY_URL}/api`;

function headers(): HeadersInit {
  const token = Cookies.get("token");
  return token ? { Authorization: `Bearer ${token}`, "Content-Type": "application/json"} : {"Content-Type": "application/json"};
}

export interface UpdateProfileFields {
  username?: string;
  name?: string;
  biography?: string;
  link?: string;
  location?: string;
}

export async function updateProfile(
  fields: UpdateProfileFields,
): Promise<void> {
  const res = await fetch(`${API_BASE}/profile/update`, {
    method: "PUT",
    headers: authHeaders({"Content-Type": "application/json"}),
    body: JSON.stringify(fields),
  });

  if (!res.ok) {
    const body = await res.json().catch(() => null);
    throw new Error(body?.message ?? "Failed to update profile");
  }
}

export async function postChangeProfilePicture(
    userId: string,
    files: File[],
    token: string,
    oldId: string
): Promise<void> {
  const formData = new FormData();

  formData.append("userId", userId);
  if (oldId) {
    formData.append("oldId", oldId);
  }
  files.forEach((file) => formData.append("files[]", file));

  const res = await fetch(API_BASE + "/change-profile-picture", {
    method: 'POST',
    headers: headers(),
    body: formData,
  });

  if (!res.ok) {
    const body = await res.json().catch(() => null);
    throw new Error(body?.message ?? "Could not update content flag");
  }

  return;
}

export async function searchProfiles(
    query: string,
) {
  const res = await fetch(API_BASE + "/search-profiles/" + query, {
    method: 'GET',
    headers: authHeaders()
  });

  if (!res.ok) throw new Error("Failed to fetch results");

  return await res.json();
}

export async function deleteOwnProfile(
  userId: string,
) {
  const res = await fetch(API_BASE + "/delete-own-profile/" + userId, {
    method: 'DELETE',
    headers: headers()
  })


  if (!res.ok) {
    const body = await res.json().catch(() => null);
    throw new Error(body?.message ?? "Could not delete your account");
  }
  const result = await res.json();
  if (result == false) {
    throw new Error("Could not finalize the deletion of your account");
  }

  return;
}
