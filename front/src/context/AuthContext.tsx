// src/context/AuthContext.tsx
"use client";

import { fetchApi } from "@/lib/client";
import { createContext, useContext, useState, useEffect, ReactNode, useCallback, useRef, useMemo } from "react";

type User = {
  userId: number;
  email: string;
  level: number;
};

type AuthContextType = {
  user: User | null;
  cartCount: number;
  isLoading: boolean;
  refetch: () => Promise<void>;
  updateCartCount: (count: number) => void;
};

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [cartCount, setCartCount] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  
  // 중복 호출 방지를 위한 ref
  const fetchingRef = useRef(false);

  // useCallback으로 메모이제이션하여 함수 재생성 방지
  const fetchAuthStatus = useCallback(async () => {
    // 이미 fetching 중이면 중단
    if (fetchingRef.current) {
      console.log('[AuthContext] 이미 fetching 중, 스킵');
      return;
    }

    fetchingRef.current = true;
    setIsLoading(true);
    
    try {
      const userRes = await fetchApi("/api/users/my", { method: "GET" });
      if (userRes.data) {
        setUser(userRes.data);        
        // 장바구니 조회
        const cartRes = await fetchApi("/api/carts", { method: "GET" });
        setCartCount(cartRes.data.cartItems.length || 0);
      } else {
        setUser(null);
        setCartCount(0);
      }
    } catch (error) {
      setUser(null);
      setCartCount(0);
    } finally {
      setIsLoading(false);
      fetchingRef.current = false;
    }
  }, []); // 의존성 없음 - 항상 동일한 함수 참조 유지

  const updateCartCount = useCallback((count: number) => {
    setCartCount(count);
  }, []);

  // 초기 마운트 시에만 실행
  useEffect(() => {
    fetchAuthStatus();
  }, [fetchAuthStatus]);

  const value = useMemo(
      () => ({ user, cartCount, isLoading, refetch: fetchAuthStatus, updateCartCount }),
      [user, cartCount, isLoading, fetchAuthStatus, updateCartCount]
    );
    
    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
  }

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}