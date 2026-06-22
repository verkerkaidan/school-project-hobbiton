import Cookies from "js-cookie";

const GATEWAY_URL = process.env.NEXT_PUBLIC_MIDDLEWARE_URL_CLIENTSIDE;

const BASE_URL = `${GATEWAY_URL}/api`;

export async function register(
  email: string,
  password: string,
  username: string,
  name: string,
): Promise<{ token?: string; userId?: string; profile?: any }> {
  const res = await fetch(`${BASE_URL}/register`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({
      email,
      password,
      username,
      name,
    }),
  });

  if (!res.ok) {
    const errorData = await res.json().catch(() => null);

    throw new Error(
      errorData?.message ?? `Registration failed (${res.status})`,
    );
  }

  const data = await res.json();

  // Persist session info in cookies for the client-side application
  if (data?.token) {
    // set cookie for token and userId (expires in 7 days)
    Cookies.set("token", data.token, { expires: 7 });
  }
  if (data?.userId) {
    Cookies.set("userId", data.userId.toString(), { expires: 7 });
  }

  return data;
}
