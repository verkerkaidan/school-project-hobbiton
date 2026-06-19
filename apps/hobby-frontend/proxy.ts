// middleware.ts  (root of your project, next to app/)
import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";
import { isTokenExpired } from "@/lib/jwt";

const PUBLIC_ROUTES = ["/authentication"];

export function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;

  const token = request.cookies.get("token")?.value;
  const userId = request.cookies.get("userId")?.value;

  const isPublicRoute = PUBLIC_ROUTES.includes(pathname);

  // Token present but expired → clear cookies and redirect with flag
  if (token && isTokenExpired(token)) {
    const url = new URL("/authentication", request.url);
    url.searchParams.set("expired", "true");
    const response = NextResponse.redirect(url);
    response.cookies.delete("token");
    response.cookies.delete("userId");
    response.cookies.delete("email");
    return response;
  }

  const isAuthenticated = !!token && !!userId;

  // Not logged in → always go to /authentication
  if (!isAuthenticated && !isPublicRoute) {
    return NextResponse.redirect(new URL("/authentication", request.url));
  }

  // Logged in → block access to /authentication
  if (isAuthenticated && isPublicRoute) {
    return NextResponse.redirect(new URL("/", request.url));
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/((?!_next/static|_next/image|favicon.ico|.*\\.png$).*)"],
};