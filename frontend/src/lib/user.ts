export interface CurrentUser {
  userId: number;
  nickname: string;
  profileImage: string | null;
  email: string;
}

export function getStoredUser(): CurrentUser | null {
  if (typeof window === "undefined") return null;
  try {
    const raw = localStorage.getItem("user");
    if (!raw) return null;
    return JSON.parse(raw) as CurrentUser;
  } catch {
    return null;
  }
}

export function setStoredUser(user: CurrentUser | null): void {
  if (typeof window === "undefined") return;
  try {
    if (user) {
      localStorage.setItem("user", JSON.stringify(user));
    } else {
      localStorage.removeItem("user");
    }
  } catch {
    // ignore
  }
}
