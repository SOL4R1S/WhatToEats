// 앱 전용 API 클라이언트.
// 웹 프론트(frontend/src/lib/api.ts)가 쿠키(reissue 재시도)로 처리하던 인증을,
// 앱에서는 토큰을 SecureStore에 저장하고 Authorization 헤더로 전달한다.

import { tokenStore } from "./auth-token";
import type { RsData } from "./types";

/** 앱에서 사용할 백엔드 base URL. EAS 환경변수 또는 기본값 (로컬 dev). */
export const API_BASE = process.env.EXPO_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

const AUTH_PATH_PREFIX = "/api/v1/auth/";
export { AUTH_PATH_PREFIX };

let reissuePromise: Promise<boolean> | null = null;

async function rawFetch(path: string, options?: RequestInit): Promise<Response> {
  const accessToken = await tokenStore.getAccessToken();
  const headers: Record<string, string> = { ...(options?.headers as Record<string, string> | undefined) };

  if (options?.body && !(typeof options.body === "string")) {
    headers["Content-Type"] = "application/json";
  }
  if (accessToken) {
    headers["Authorization"] = `Bearer ${accessToken}`;
  }

  return fetch(`${API_BASE}${path}`, { ...options, headers });
}

/** refreshToken으로 신규 accessToken을 발급받아 저장. 동시 요청 1회만 발급. */
async function reissueAccessToken(): Promise<boolean> {
  if (!reissuePromise) {
    reissuePromise = (async () => {
      const refreshToken = await tokenStore.getRefreshToken();
      if (!refreshToken) return false;
      try {
        const res = await rawFetch(`${AUTH_PATH_PREFIX}reissue`, {
          method: "POST",
          headers: { Authorization: `Bearer ${refreshToken}` },
        });
        if (!res.ok) return false;
        const json = (await res.json()) as RsData<{ accessToken: string; refreshToken: string }>;
        const data = json?.data;
        if (!data?.accessToken || !data?.refreshToken) return false;
        await tokenStore.setTokens(data.accessToken, data.refreshToken);
        return true;
      } catch {
        return false;
      } finally {
        reissuePromise = null;
      }
    })();
  }
  return reissuePromise;
}

function isAuthPath(path: string): boolean {
  return path.startsWith(AUTH_PATH_PREFIX);
}

export async function apiFetch(path: string, options?: RequestInit, retried = false): Promise<Response> {
  const res = await rawFetch(path, options);

  // 인증 만료(401) 시 재발급 후 1회 재시도
  if (res.status === 401 && !isAuthPath(path) && !retried) {
    const reissued = await reissueAccessToken();
    if (reissued) {
      return apiFetch(path, options, true);
    }
  }
  return res;
}

export async function apiFetchJson<T = unknown>(
  path: string,
  options?: RequestInit,
): Promise<{ ok: boolean; data?: T; message?: string }> {
  const res = await apiFetch(path, options);
  const json = (await res.json().catch(() => ({}))) as RsData<T>;
  if (!res.ok) {
    return { ok: false, message: json?.message || "요청에 실패했습니다." };
  }
  return { ok: true, data: json.data, message: json.message };
}