"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter, usePathname } from "next/navigation";

const getCookie = (name: string) => {
  if (typeof document === "undefined") return null;
  const match = document.cookie.match(new RegExp("(^| )" + name + "=([^;]+)"));
  return match ? decodeURIComponent(match[2]) : null;
};

export default function Header() {
  const [isLoggedIn, setIsLoggedIn] = useState<boolean | null>(null);
  const router = useRouter();
  const pathname = usePathname(); // ✅ 현재 경로 감지

  // ✅ 경로가 바뀔 때마다 쿠키 다시 읽기
  useEffect(() => {
    const token = getCookie("accessToken");
    setIsLoggedIn(!!token);
  }, [pathname]); // ✅ 경로가 바뀔 때마다 재실행

  const handleLogout = async () => {
    // 1️⃣ 쿠키 삭제
    document.cookie = "accessToken=; Max-Age=0; path=/;";

    // 2️⃣ 상태 즉시 반영
    setIsLoggedIn(false);

    // 3️⃣ 알림 및 페이지 이동
    alert("로그아웃 되었습니다 👋");

    // 4️⃣ 홈으로 이동 후 새로고침
    router.push("/home");
    setTimeout(() => router.refresh(), 100); // ✅ 짧은 delay 주면 확실함
  };

  if (isLoggedIn === null) {
    return (
      <header className="flex justify-between items-center px-6 py-4 bg-white shadow-sm">
        <div className="text-gray-500 text-sm">상태 확인 중...</div>
      </header>
    );
  }

  return (
    <header className="flex justify-between items-center px-6 py-4 bg-white shadow-sm">
      <Link href="/home" className="text-xl font-bold text-blue-600">
        🐾 PinCo
      </Link>

      <nav className="flex gap-4 items-center">
        {isLoggedIn ? (
          <>
            <Link href="/profile" className="text-gray-700 hover:text-blue-600">
              프로필
            </Link>
            <button
              onClick={handleLogout}
              className="text-gray-700 hover:text-red-500"
            >
              로그아웃
            </button>
          </>
        ) : (
          <>
            <Link href="/user/login" className="text-gray-700 hover:text-blue-600">
              로그인
            </Link>
            <Link href="/user/join" className="text-gray-700 hover:text-blue-600">
              회원가입
            </Link>
          </>
        )}
      </nav>
    </header>
  );
}
