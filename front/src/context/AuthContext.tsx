"use client";

import React, { createContext, useContext, useEffect, useState } from "react";
import { useRouter } from "next/navigation";

type AuthContextType = {
  isLoggedIn: boolean;
  login: (token: string) => void;
  logout: () => void;
};

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const router = useRouter();

  // ✅ 쿠키 읽기 함수 (전역)
  const getCookie = (name: string) => {
    if (typeof document === "undefined") return null;
    const match = document.cookie.match(new RegExp("(^| )" + name + "=([^;]+)"));
    return match ? decodeURIComponent(match[2]) : null;
  };

  // ✅ 첫 렌더 시 로그인 상태 확인
  useEffect(() => {
    const token = getCookie("accessToken");
    setIsLoggedIn(!!token);
  }, []);

  const login = (token: string) => {
    document.cookie = `accessToken=${token}; path=/;`;
    setIsLoggedIn(true);
  };

  const logout = () => {
    document.cookie = "accessToken=; Max-Age=0; path=/;";
    setIsLoggedIn(false);
    alert("로그아웃 되었습니다 👋");
    router.push("/user/login");
  };

  return (
    <AuthContext.Provider value={{ isLoggedIn, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
};
