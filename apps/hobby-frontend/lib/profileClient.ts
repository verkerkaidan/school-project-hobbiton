const GATEWAY_URL =
  process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE ?? "http://localhost:23738";

const API_BASE = `${GATEWAY_URL}/api`;

/**
 * Creates a profile in the profile-service via the API gateway.
 * Must be called after registration, using the JWT token saved to cookies.
 * @param username Unique username chosen by the user
 * @param name     Optional display name
 * @param token    Bearer token issued by the auth service
 */
export async function createProfile(
  username: string,
  name: string,
  token: string,
): Promise<void> {
  const res = await fetch(`${API_BASE}/profile/create`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ username, name }),
  });

  if (!res.ok) {
    const data = await res.json().catch(() => null);
    throw new Error(data?.message ?? `Profile creation failed (${res.status})`);
  }
}
