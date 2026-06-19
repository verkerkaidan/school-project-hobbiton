import Cookies from "js-cookie";

const BASE_URL = process.env.NEXT_PUBLIC_AUTHENTICATION_URL;
console.log("[authClient] BASE_URL =", BASE_URL);

export interface AuthResponse {
  token: string;
  userId: string;
}

export interface AuthError {
  message: string;
}

/**
 * Actually makes sure that the response is OK.
 * @param res Total response of request
 * @returns the data again
 */
async function handleResponse<T>(res: Response): Promise<T> {
  const data = await res.json();
  if (!res.ok)
    throw new Error((data as AuthError).message ?? "Something went wrong.");
  return data as T;
}

/**
 * Tries to log user in by fetching with post, if return is 200 goes to handleResponse and saves into cookies with savesSession()
 * If login fails, throws error
 * @param email User email for login
 * @param password User password for login
 */
export async function login(email: string, password: string): Promise<void> {
  const res = await fetch(`${BASE_URL}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });

  if (res.status == 200) {
    const data = await handleResponse<AuthResponse>(res);
    saveSession(data, email);
  } else {
    // Parse the error body if your API returns one, fallback to a generic message
    const errorData = await res.json().catch(() => null);
    const message = errorData?.message ?? `Login failed (${res.status})`;
    throw new Error(message);
  }
}

/**
 * Returns the email address of the currently authenticated user.
 * The email is persisted in a cookie at login/register time since the JWT
 * does not carry an email claim.
 */
export function getCurrentUserEmail(): string | null {
  return Cookies.get("email") ?? null;
}

/**
 * Updates the email address for the currently authenticated user.
 */
export async function updateEmail(newEmail: string): Promise<void> {
  const token = Cookies.get("token");
  if (!token) throw new Error("Not authenticated");

  const res = await fetch(`${BASE_URL}/api/auth/email`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ email: newEmail }),
  });

  if (!res.ok) {
    const data = await res.json().catch(() => null);
    const message =
      (data as AuthError)?.message ?? `Email update failed (${res.status})`;
    throw new Error(message);
  }
}

/**
 * Changes the password for the currently authenticated user.
 * Requires the current password and a new password.
 */
export async function changePassword(
  currentPassword: string,
  newPassword: string
): Promise<void> {
  const token = Cookies.get("token");
  if (!token) throw new Error("Not authenticated");

  const res = await fetch(`${BASE_URL}/api/auth/password`, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ currentPassword, newPassword }),
  });

  if (!res.ok) {
    const data = await res.json().catch(() => null);
    const message =
      (data as AuthError)?.message ?? `Password change failed (${res.status})`;
    throw new Error(message);
  }
}

/**
 * Saves token, userId, and email into cookies for 2 days
 * @param res total result of auth requests
 * @param email the email used to authenticate
 */
function saveSession(res: AuthResponse, email: string) {
  Cookies.set("token", res.token, { expires: 2 });
  Cookies.set("userId", res.userId, { expires: 2 });
  Cookies.set("email", email, { expires: 2 });
}
