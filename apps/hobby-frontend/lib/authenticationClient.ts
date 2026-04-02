import Cookies from "js-cookie";

const BASE_URL = process.env.NEXT_PUBLIC_AUTHENTICATION_URL;

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
    if (!res.ok) throw new Error((data as AuthError).message ?? "Something went wrong.");
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
        saveSession(data);
    } else {
        // Parse the error body if your API returns one, fallback to a generic message
        const errorData = await res.json().catch(() => null);
        const message = errorData?.message ?? `Login failed (${res.status})`;
        throw new Error(message);
    }

}
/**
 * Tries to register user by fetching with post, if return is 200 goes to handleResponse and saves into cookies with savesSession()
 * If register fails, throws error
 * @param email User email for login
 * @param password User password for login
 */
export async function register(email: string, password: string): Promise<void> {
    const res = await fetch(`${BASE_URL}/api/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
    });

    if (res.status == 200) {
        const data = await handleResponse<AuthResponse>(res);
        saveSession(data);
    } else {
        // Parse the error body if your API returns one, fallback to a generic message
        const errorData = await res.json().catch(() => null);
        const message = errorData?.message ?? `Login failed (${res.status})`;
        throw new Error(message);
    }
}

/**
 * Saves token and userId into cookies for 2 days
 * @param res total result of auth requests 
 */
function saveSession(res: AuthResponse) {
    Cookies.set("token", res.token, { expires: 2 });
    Cookies.set("userId", res.userId, { expires: 2 });
}