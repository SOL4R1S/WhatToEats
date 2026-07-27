export const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL ?? "";

export function getImageUrl(url?: string | null): string | null {
  if (!url) return null;

  if (url.startsWith("/uploads/")) return url;

  try {
    const parsedUrl = new URL(url);
    if (parsedUrl.pathname.startsWith("/uploads/")) {
      return `${parsedUrl.pathname}${parsedUrl.search}`;
    }
  } catch {
    return url;
  }

  return url;
}

const protectedPathPrefixes = [
  "/feed",
  "/profile",
  "/search",
  "/recommend",
  "/lists",
  "/restaurant",
] as const;

function clearClientSession() {
  localStorage.removeItem("isLoggedIn");
  localStorage.removeItem("user");
  window.dispatchEvent(new Event("login-state-change"));
}

function shouldRedirectToLogin(status: number) {
  if (typeof window === "undefined" || status !== 401) return false;
  return protectedPathPrefixes.some((prefix) => window.location.pathname.startsWith(prefix));
}

export async function apiFetch(path: string, options?: RequestInit) {
  const isFormData = options?.body instanceof FormData;

  return fetch(`${API_BASE}${path}`, {
    credentials: "include",
    ...options,
    headers: {
      ...(options?.body && !isFormData ? { "Content-Type": "application/json" } : {}),
      ...options?.headers,
    },
  });
}

export async function apiFetchJson<T = unknown>(path: string, options?: RequestInit): Promise<{ ok: boolean; data?: T; message?: string }> {
  const res = await apiFetch(path, options);
  const json = await res.json().catch(() => ({}));

  if (!res.ok) {
    if (shouldRedirectToLogin(res.status)) {
      clearClientSession();
      window.location.assign("/login");
    }

    return { ok: false, message: json.message || "요청에 실패했습니다." };
  }

  return { ok: true, data: json.data, message: json.message };
}
