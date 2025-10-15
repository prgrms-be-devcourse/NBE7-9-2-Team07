"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useAuth } from "@/context/AuthContext"; // ✅ 추가

export default function Header() {
  const { isLoggedIn, logout } = useAuth(); // ✅ 전역 상태 가져오기
  const pathname = usePathname();

  return (
    <header className="flex justify-between items-center px-6 py-4 bg-white shadow-sm">
      <Link
        href="/home"
        className={`text-xl font-bold ${
          pathname === "/home" ? "text-blue-600" : "text-gray-700"
        }`}
      >
        🐾 PinCo
      </Link>

      <nav className="flex gap-4 items-center">
        {isLoggedIn ? (
          <>
            <Link href="/profile" className="text-gray-700 hover:text-blue-600">
              프로필
            </Link>
            <button
              onClick={logout}
              className="text-gray-700 hover:text-red-500"
            >
              로그아웃
            </button>
          </>
        ) : (
          <>
            <Link
              href="/user/login"
              className="text-gray-700 hover:text-blue-600"
            >
              로그인
            </Link>
            <Link
              href="/user/join"
              className="text-gray-700 hover:text-blue-600"
            >
              회원가입
            </Link>
          </>
        )}
      </nav>
    </header>
  );
}
