import * as SecureStore from "expo-secure-store";
import type { AuthUserResponse } from "./types";

const ACCESS_TOKEN_KEY = "whattoeat.accessToken";
const REFRESH_TOKEN_KEY = "whattoeat.refreshToken";
const USER_KEY = "whattoeat.user";

/** 앱 전용 인증 저장소 (SecureStore 기반 — 웹의 HttpOnly 쿠키를 대체) */
export const tokenStore = {
  async getAccessToken(): Promise<string | null> {
    return SecureStore.getItemAsync(ACCESS_TOKEN_KEY);
  },
  async getRefreshToken(): Promise<string | null> {
    return SecureStore.getItemAsync(REFRESH_TOKEN_KEY);
  },
  async setTokens(access: string, refresh: string): Promise<void> {
    await SecureStore.setItemAsync(ACCESS_TOKEN_KEY, access);
    await SecureStore.setItemAsync(REFRESH_TOKEN_KEY, refresh);
  },
  async clear(): Promise<void> {
    await SecureStore.deleteItemAsync(ACCESS_TOKEN_KEY);
    await SecureStore.deleteItemAsync(REFRESH_TOKEN_KEY);
    await SecureStore.deleteItemAsync(USER_KEY);
  },
  async setUser(user: AuthUserResponse): Promise<void> {
    await SecureStore.setItemAsync(USER_KEY, JSON.stringify(user));
  },
  async getUser(): Promise<AuthUserResponse | null> {
    const raw = await SecureStore.getItemAsync(USER_KEY);
    return raw ? (JSON.parse(raw) as AuthUserResponse) : null;
  },
};