/**
 * Lightweight JWT payload decoder (no verification — that's the gateway's job).
 * Reads the `roles` claim to determine admin status on the frontend.
 *
 * MOCK NOTE: The auth service does not yet include a `roles` claim in the JWT.
 * Until it does, `getJwtRoles` will always return [] and `isAdminToken` will
 * always be false. The logic is in place — the auth-service author just needs
 * to add `.withClaim("roles", user.getRoles())` to JWTUtil#generateToken.
 */

/** Decodes the payload section of a JWT without verifying the signature. */
export function decodeJwtPayload(
  token: string,
): Record<string, unknown> | null {
  try {
    const parts = token.split(".");
    if (parts.length !== 3) return null;
    // Base64url → Base64 → JSON
    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const json = atob(base64);
    return JSON.parse(json) as Record<string, unknown>;
  } catch {
    return null;
  }
}

/**
 * Returns the `roles` array from the JWT payload.
 * Expected shape once the auth service adds it: `{ "roles": ["ROLE_USER"] }`
 * or `{ "roles": ["ROLE_ADMIN", "ROLE_USER"] }`.
 */
export function getJwtRoles(token: string): string[] {
  const payload = decodeJwtPayload(token);
  if (!payload) return [];
  // Use the `role` claim only (the auth service emits `role` as a list)
  const roles = payload["role"];
  if (Array.isArray(roles)) return roles as string[];
  // Some implementations emit a single string instead of an array
  if (typeof roles === "string") return [roles];
  return [];
}

/** True when the JWT contains the ROLE_ADMIN authority. */
export function isAdminToken(token: string): boolean {
  return getJwtRoles(token).includes("ROLE_ADMIN");
}

/** True when the JWT `exp` claim is in the past (or the token is malformed). */
export function isTokenExpired(token: string): boolean {
  const payload = decodeJwtPayload(token);
  if (!payload || typeof payload["exp"] !== "number") return true;
  return Date.now() >= payload["exp"] * 1000;
}
