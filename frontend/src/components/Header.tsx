"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { LogOut, User } from "lucide-react";
import { CurrentUser, getStoredUser } from "@/lib/user";
import { getImageUrl } from "@/lib/api";

const protectedPaths = ["/feed", "/profile", "/search", "/recommend", "/lists", "/restaurant"];

const navItems = [
  { href: "/feed", label: "피드" },
  { href: "/search", label: "검색" },
  { href: "/recommend", label: "추천" },
  { href: "/lists", label: "리스트" },
  { href: "/profile", label: "프로필" },
];

export default function Header() {
  const router = useRouter();
  const pathname = usePathname();
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState<CurrentUser | null>(null);

  useEffect(() => {
    const checkLoginStatus = () => {
      const logged = localStorage.getItem("isLoggedIn") === "true";
      const storedUser = getStoredUser();
      setIsLoggedIn(logged);
      if (logged && storedUser) {
        setUser(storedUser);
      } else {
        setUser(null);
        // 보호된 페이지일 때 로그인 페이지로 리다이렉트
        if (pathname !== "/login" && protectedPaths.some((p) => pathname?.startsWith(p))) {
          router.push("/login");
        }
      }
    };

    checkLoginStatus();
    window.addEventListener("storage", checkLoginStatus);
    window.addEventListener("login-state-change", checkLoginStatus);

    return () => {
      window.removeEventListener("storage", checkLoginStatus);
      window.removeEventListener("login-state-change", checkLoginStatus);
    };
  }, [pathname, router]);

  // 카카오 로그인 클릭 시 가상 로그인 화면(/login)으로 라우팅 처리
  const handleLoginClick = () => {
    router.push("/login");
  };

  // 로그아웃
  const handleLogout = async () => {
    try {
      await fetch("/api/v1/auth/logout", {
        method: "POST",
        credentials: "include",
      });
    } catch {
      // 서버 요청 실패핏 localStorage 클리어 및 이동
    } finally {
      localStorage.removeItem("isLoggedIn");
      localStorage.removeItem("user");
      setIsLoggedIn(false);
      setUser(null);
      window.dispatchEvent(new Event("login-state-change"));
      router.push("/");
    }
  };

  return (
    <header className="sticky top-0 z-10 border-b border-hairline-soft bg-background/95 backdrop-blur">
      <div className="mx-auto flex h-[72px] max-w-5xl items-center justify-between px-6">
        <Link href="/" className="text-xl font-semibold tracking-tight text-ink">
          오늘뭐먹지
        </Link>
        
        <div className="hidden items-center gap-6 md:flex">
          {/* 네비게이션: 로그인 시에만 노출 */}
          {isLoggedIn && (
            <nav className="flex gap-6 text-base font-semibold">
              {navItems.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className="text-muted transition-colors hover:text-ink"
                >
                  {item.label}
                </Link>
              ))}
            </nav>
          )}

          {/* 로그인 상태 분기 UI */}
          {isLoggedIn && user ? (
            <div className="flex items-center gap-3 border-l border-hairline-soft pl-6">
              {getImageUrl(user.profileImage) ? (
                <img
                  src={getImageUrl(user.profileImage) ?? "/default-profile.png"}
                  alt="사용자 프로필"
                  className="h-8 w-8 rounded-full object-cover ring-2 ring-primary/20"
                />
              ) : (
                <div className="flex h-8 w-8 items-center justify-center rounded-full bg-surface-strong ring-2 ring-primary/20">
                  <User className="h-4 w-4 text-muted" />
                </div>
              )}
              <span className="text-sm font-bold text-ink">
                {user.nickname} <span className="font-medium text-muted">님</span>
              </span>
              <button 
                onClick={handleLogout}
                className="ml-2 flex items-center gap-1 rounded-lg border border-hairline bg-white px-2.5 py-1.5 text-xs font-semibold text-muted transition-colors hover:bg-surface-soft hover:text-ink"
                title="로그아웃"
              >
                <LogOut className="h-3 w-3" />
                로그아웃
              </button>
            </div>
          ) : (
            <button 
              onClick={handleLoginClick}
              className="rounded-lg bg-primary px-4 py-2 text-sm font-bold text-white shadow-sm transition-colors hover:bg-primary-active cursor-pointer"
            >
              로그인
            </button>
          )}
        </div>
      </div>
    </header>
  );
}
