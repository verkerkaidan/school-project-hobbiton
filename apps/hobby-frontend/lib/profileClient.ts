const GATEWAY_URL =
  process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE ?? "http://localhost:23738";
const API_BASE = `${GATEWAY_URL}/api`;

interface AuthResponse {
  token: string;
}

interface JWTPayload {
  exp?: number;
  [key: string]: unknown;
}

export async function registerProfile(
  email: string,
  password: string,
  username: string,
  name: string,
): Promise<void> {
  const token = await createAuthentication(email, password);

  // Check if token is correct: must be a valid, non-expired JWT
  if (!isValidToken(token)) {
    throw new Error("Received an invalid or expired token from the auth service.");
  }

  await createProfile(username, name, token);

  // Check if profile exists after creation; if not, rollback the auth account
  const profileCreated = await checkProfileExists(username, token);
  if (!profileCreated) {
    await deleteAuthentication(token).catch(() => {
      // Swallow rollback errors — log them but don't mask the original failure
      console.error(`Rollback failed: auth account for "${email}" may need manual cleanup.`);
    });
    throw new Error(`Profile creation could not be confirmed for "${username}". Auth account has been rolled back.`);
  }
}

/**
 * Validates a JWT string: checks it is non-empty and not expired.
 * Does NOT verify the signature (that is the gateway's responsibility).
 */
function isValidToken(token: string): boolean {
  if (!token || typeof token !== "string") return false;

  const parts = token.split(".");
  if (parts.length !== 3) return false;

  try {
    const payload: JWTPayload = JSON.parse(atob(parts[1]));
    if (payload.exp !== undefined && payload.exp * 1000 < Date.now()) {
      return false; // Token already expired
    }
    return true;
  } catch {
    return false; // Malformed payload
  }
}

/**
 * Checks whether the profile was successfully persisted after creation.
 * Returns true on 200, false on 404, and throws on unexpected errors.
 */
async function checkProfileExists(username: string, token: string): Promise<boolean> {
  const res = await fetch(`${API_BASE}/profile/${encodeURIComponent(username)}`, {
    method: "GET",
    headers: { Authorization: `Bearer ${token}` },
  });

  if (res.ok) return true;
  if (res.status === 404) return false;

  const data = await res.json().catch(() => null);
  throw new Error(data?.message ?? `Profile check failed (${res.status})`);
}

/**
 * Deletes the auth account. Called as a rollback when profile creation
 * cannot be confirmed, to avoid leaving a dangling credential.
 */
async function deleteAuthentication(token: string): Promise<void> {
  const res = await fetch(`${API_BASE}/auth/delete`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });

  if (!res.ok) {
    const data = await res.json().catch(() => null);
    throw new Error(data?.message ?? `Auth rollback failed (${res.status})`);
  }
}

async function createAuthentication(
  email: string,
  password: string,
): Promise<string> {
  const res = await fetch(`${API_BASE}/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });

  if (!res.ok) {
    const data = await res.json().catch(() => null);
    throw new Error(data?.message ?? `Authentication failed (${res.status})`);
  }

  const data: AuthResponse = await res.json();
  return data.token;
}

async function createProfile(
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