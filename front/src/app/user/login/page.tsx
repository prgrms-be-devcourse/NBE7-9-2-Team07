"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";

export default function LoginPage() {
  const router = useRouter();
  const { login } = useAuth();
  const [email, setEmail] = useState("user1@example.com");
  const [password, setPassword] = useState("12345678");

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const ok = await login(email, password);
    if (ok) {
      alert("로그인 성공 🎉");
      router.push("/home");
    } else {
      alert("로그인 실패 ❌");
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-3 w-80 mx-auto mt-40">
      <input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="이메일" className="border rounded p-2" />
      <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="비밀번호" className="border rounded p-2" />
      <button className="bg-blue-600 text-white rounded p-2">로그인</button>

      <div className="text-sm text-gray-500 text-center mt-2">
        <p>기본 계정:</p>
        <p>user1@example.com / 12345678</p>
        <p>user2@example.com / 12341234</p>
      </div>
    </form>
  );
}
