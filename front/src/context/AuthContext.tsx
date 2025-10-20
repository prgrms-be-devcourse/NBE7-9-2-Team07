"use client";
import { createContext, useContext, useEffect, useState } from "react";

type User = { id: number; email: string; name?: string } | null;

const AuthContext = createContext<{
  user: User;
  login: (email: string, password: string) => Promise<boolean>;
  logout: () => void;
}>({
  user: null,
  login: async () => false,
  logout: () => {},
});

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<User>(null);

  useEffect(() => {
    const saved = localStorage.getItem("user");
    if (saved) setUser(JSON.parse(saved));
  }, []);

  const login = async (email: string, password: string) => {
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/user/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password }),
      });

      const data = await res.json();

      if (data.errorCode === "200") {
        const userRes = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/user/check/1`);
        const userData = await userRes.json();

        const loggedUser = { id: userData.id ?? 1, email, name: userData.userName ?? "User" };
        localStorage.setItem("user", JSON.stringify(loggedUser));
        setUser(loggedUser);
        return true;
      }
    } catch (err) {
      console.error(err);
    }
    return false;
  };

  const logout = () => {
    localStorage.removeItem("user");
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
