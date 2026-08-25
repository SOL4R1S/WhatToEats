import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import { AUTH_PATH_PREFIX, apiFetchJson } from "@/lib/api";
import { tokenStore } from "@/lib/auth-token";
import type { AuthUserResponse, LoginRequest, SignUpRequest } from "@/lib/types";

interface AuthContextValue {
  token: string | null;
  user: AuthUserResponse | null;
  initializing: boolean;
  login: (req: LoginRequest) => Promise<string | null>;
  signup: (req: SignUpRequest) => Promise<string | null>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<AuthUserResponse | null>(null);
  const [initializing, setInitializing] = useState(true);

  // 부팅 시 저장된 토큰/사용자 로드
  useEffect(() => {
    (async () => {
      const t = await tokenStore.getAccessToken();
      const u = await tokenStore.getUser();
      setToken(t);
      setUser(u);
      setInitializing(false);
    })();
  }, []);

  const login = async (req: LoginRequest): Promise<string | null> => {
    // NOTE: 현행 백엔드 /auth/login, /auth/signup은 HttpOnly 쿠키에만 토큰을 심어
    // 응답 body에 토큰이 없으므로, 앱은 프로필만 저장하고 세션 마커로 처리한다.
    // 실제 토큰 기반 통신은 "앱용 API"(로그인 응답에 accessToken/refreshToken을 JSON으로
    // 반환)가 백엔드에 추가된 뒤 활성화된다. 그 전까지 앱과 백엔드는 쿠키 미지원이므로
    // 로그인은 확인용이고, 보호 API 호출은 앱용 API 도입을 기다린다.
    const { ok, data, message } = await apiFetchJson<AuthUserResponse>(`${AUTH_PATH_PREFIX}login`, {
      method: "POST",
      body: JSON.stringify(req),
    });
    if (!ok || !data) return message ?? "로그인 실패";
    await tokenStore.setUser(data);
    setUser(data);
    setToken("session");
    return null;
  };

  const signup = async (req: SignUpRequest): Promise<string | null> => {
    const { ok, data, message } = await apiFetchJson<AuthUserResponse>(`${AUTH_PATH_PREFIX}signup`, {
      method: "POST",
      body: JSON.stringify(req),
    });
    if (!ok || !data) return message ?? "회원가입 실패";
    await tokenStore.setUser(data);
    setUser(data);
    setToken("session");
    return null;
  };

  const logout = async () => {
    await apiFetchJson(`${AUTH_PATH_PREFIX}logout`, { method: "POST" });
    await tokenStore.clear();
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ token, user, initializing, login, signup, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}