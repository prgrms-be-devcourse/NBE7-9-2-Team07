"use client";

import { MapPin } from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";

export default function Header() {
  const pathname = usePathname();

  // ✅ 로그인과 회원가입 페이지만 헤더 숨김
  const hideHeader = pathname === "/user/login" || pathname === "/user/join";

  if (hideHeader) return null;

  return (
    <header className="bg-white border-b sticky top-0 z-50">
      <div className="max-w-7xl mx-auto flex justify-between items-center p-4">
        <Link href="/home" className="flex items-center gap-2 cursor-pointer">
          <MapPin className="text-blue-600 w-6 h-6" />
          <h1 className="text-blue-600 text-lg font-semibold">PinCo</h1>
        </Link>

        <nav className="flex items-center gap-3">
          <Link href="/user/login" className="text-gray-700 hover:text-blue-600">
            로그인
          </Link>
          <Link
            href="/user/join"
            className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
          >
            회원가입
          </Link>
        </nav>
      </div>
    </header>
  );
}
