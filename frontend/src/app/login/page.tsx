"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { UserRound, ShieldCheck, RefreshCw } from "lucide-react";
import { apiFetchJson } from "@/lib/api";

// 기기 고유 익명 키를 로컬에 생성·유지 (그린메일: 이메일/비번 수집 없음)
function getOrCreateDeviceKey(): string {
  const KEY = "whattoeat.deviceKey";
  let key = localStorage.getItem(KEY);
  if (!key) {
    key = crypto.randomUUID() + crypto.randomUUID();
    localStorage.setItem(KEY, key);
  }
  return key;
}

export default function LoginPage() {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showAdmin, setShowAdmin] = useState(false);
  const [adminId, setAdminId] = useState("");
  const [adminPw, setAdminPw] = useState("");

  const startAnonymous = async () => {
    setLoading(true);
    setError(null);
    const { ok, data, message } = await apiFetchJson<any>("/api/v1/auth/anonymous", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ deviceKey: getOrCreateDeviceKey() }),
    });
    if (ok && data?.userProfile) {
      localStorage.setItem("isLoggedIn", "true");
      localStorage.setItem("user", JSON.stringify(data.userProfile));
      router.replace("/feed");
    } else {
      setError(message ?? "익명 계정 생성에 실패했습니다.");
      setLoading(false);
    }
  };

  const loginAdmin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    const { ok, data, message } = await apiFetchJson<any>("/api/v1/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ loginId: adminId, password: adminPw }),
    });
    if (ok && data) {
      localStorage.setItem("isLoggedIn", "true");
      localStorage.setItem("user", JSON.stringify(data));
      router.replace("/admin");
    } else {
      setError(message ?? "로그인 실패");
    }
    setLoading(false);
  };

  return (
    <div className="relative flex min-h-screen items-center justify-center bg-gradient-to-br from-primary via-primary to-primary-active px-4">
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute -top-24 -left-24 h-80 w-80 rounded-full bg-white/10 blur-3xl" />
        <div className="absolute -bottom-24 -right-24 h-96 w-96 rounded-full bg-white/10 blur-3xl" />
      </div>

      <div className="relative z-10 w-full max-w-sm">
        <div className="rounded-3xl bg-white p-8 shadow-2xl">
          <h1 className="text-center text-3xl font-black text-primary">오늘뭐먹지</h1>
          <p className="mt-1 text-center text-sm text-muted">뭐 먹을까? 여기서 기록하고 공유하세요</p>

          <button
            onClick={startAnonymous}
            disabled={loading}
            className="mt-8 flex w-full items-center justify-center gap-2 rounded-2xl bg-primary px-6 py-4 font-bold text-white transition-transform active:scale-[0.98] disabled:opacity-70"
          >
            <UserRound className="h-5 w-5" />
            {loading ? "시작하는 중..." : "익명으로 바로 시작하기"}
          </button>

          <p className="mt-3 text-center text-xs text-muted">
            <RefreshCw className="mr-1 inline h-3 w-3" />
            가입 절차 없이 자동 닉네임이 만들어져요 · 이메일·비밀번호 수집 안 함
          </p>

          {error && <p className="mt-4 rounded-xl bg-red-50 p-3 text-center text-sm text-red-600">{error}</p>}

          <div className="mt-6 border-t border-hairline pt-4 text-center">
            <button
              onClick={() => setShowAdmin((s) => !s)}
              className="inline-flex items-center gap-1 text-xs text-muted-soft hover:text-muted"
            >
              <ShieldCheck className="h-3 w-3" /> 관리자 로그인
            </button>
          </div>

          {showAdmin && (
            <form onSubmit={loginAdmin} className="mt-4 space-y-3 border-t border-hairline pt-4">
              <input
                type="text"
                placeholder="관리자 아이디"
                value={adminId}
                onChange={(e) => setAdminId(e.target.value)}
                className="w-full rounded-xl border border-hairline bg-surface-soft px-4 py-2.5 text-sm focus:border-primary focus:outline-hidden"
                required
              />
              <input
                type="password"
                placeholder="비밀번호"
                value={adminPw}
                onChange={(e) => setAdminPw(e.target.value)}
                className="w-full rounded-xl border border-hairline bg-surface-soft px-4 py-2.5 text-sm focus:border-primary focus:outline-hidden"
                required
              />
              <button
                type="submit"
                disabled={loading}
                className="w-full rounded-xl border border-hairline py-2.5 text-sm font-bold text-ink hover:bg-surface-soft disabled:opacity-70"
              >
                관리자 로그인
              </button>
            </form>
          )}

          <div className="mt-6 flex justify-center gap-4 border-t border-hairline pt-4 text-xs text-muted-soft">
            <Link href="/legal/terms.html" className="hover:text-muted">이용약관</Link>
            <Link href="/legal/privacy.html" className="hover:text-muted">개인정보처리방침</Link>
          </div>
        </div>
      </div>
    </div>
  );
}