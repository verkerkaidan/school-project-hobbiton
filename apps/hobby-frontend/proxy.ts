// middleware.ts  (root of your project, next to app/)
import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

const PUBLIC_ROUTES = ["/authentication"];

export function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;

  const token = request.cookies.get("token")?.value;
  const userId = request.cookies.get("userId")?.value;

  const isAuthenticated = !!token && !!userId;
  const isPublicRoute = PUBLIC_ROUTES.includes(pathname);

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