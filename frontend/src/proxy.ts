import { NextResponse, type NextRequest } from "next/server";

const protectedPathPrefixes = [
  "/feed",
  "/profile",
  "/search",
  "/recommend",
  "/lists",
  "/restaurant",
] as const;

function isProtectedPath(pathname: string) {
  return protectedPathPrefixes.some((prefix) => pathname.startsWith(prefix));
}

export function proxy(request: NextRequest) {
  if (!isProtectedPath(request.nextUrl.pathname)) {
    return NextResponse.next();
  }

  if (request.cookies.has("accessToken")) {
    return NextResponse.next();
  }

  const loginUrl = new URL("/login", request.url);
  return NextResponse.redirect(loginUrl);
}

export const config = {
  matcher: [
    "/feed/:path*",
    "/profile/:path*",
    "/search/:path*",
    "/recommend/:path*",
    "/lists/:path*",
    "/restaurant/:path*",
  ],
};
