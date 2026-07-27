"use client";

import { Suspense, useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { apiFetchJson } from "@/lib/api";

function OAuthCallbackContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [error, setError] = useState("");

  useEffect(() => {
    const code = searchParams.get("code");
    if (!code) {
      router.replace("/login?error");
      return;
    }

    const exchange = async () => {
      const res = await apiFetchJson("/api/v1/auth/oauth/exchange", {
        method: "POST",
        body: JSON.stringify({ code }),
      });

      if (res.ok) {
        localStorage.setItem("isLoggedIn", "true");
        localStorage.setItem("user", JSON.stringify(res.data ?? {}));
        window.dispatchEvent(new Event("login-state-change"));
        router.replace("/feed");
      } else {
        setError(res.message || "로그인에 실패했습니다.");
        router.replace("/login?error");
      }
    };

    exchange();
  }, [router, searchParams]);

  return (
    <div className="min-h-screen flex items-center justify-center">
      <p className="text-sm text-muted-foreground">{error || "로그인 처리 중입니다..."}</p>
    </div>
  );
}

export default function OAuthCallbackPage() {
  return (
    <Suspense
      fallback={
        <div className="min-h-screen flex items-center justify-center">
          <p className="text-sm text-muted-foreground">로그인 처리 중입니다...</p>
        </div>
      }
    >
      <OAuthCallbackContent />
    </Suspense>
  );
}
