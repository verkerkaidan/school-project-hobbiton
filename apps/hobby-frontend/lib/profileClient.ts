const GATEWAY_URL =
  process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE ?? "http://localhost:23738";

const API_BASE = `${GATEWAY_URL}/api`;

export interface UpdateProfileFields {
  username?: string;
  name?: string;
  biography?: string;
  link?: string;
  location?: string;
}

export async function updateProfile(
  fields: UpdateProfileFields,
  token: string
): Promise<void> {
  const res = await fetch(`${API_BASE}/profile/update`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
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
    headers: {
      Authorization:  `Bearer ${token}`,
    },
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
  });

  if (!res.ok) throw new Error("Failed to fetch results");
  const data = await res.json();
  return data
}
