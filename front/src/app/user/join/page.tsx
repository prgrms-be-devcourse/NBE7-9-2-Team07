"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Mail, Lock, User } from "lucide-react";

export default function SignUpPage() {
  const router = useRouter();

  const [form, setForm] = useState({
    userName: "",
    password: "",
    email: "",
  });

  // 자동 로그인 여부
  const [autoLogin, setAutoLogin] = useState(true);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      // ✅ 회원가입 요청
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/user/join`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include", // 쿠키 수신
        body: JSON.stringify({
          email: form.email,
          password: form.password,
          userName: form.userName,
        }),
      });

      if (!res.ok) throw new Error(await res.text());
      alert("회원가입이 완료되었습니다 🎉");

      // ✅ 가입 후 자동 로그인
      if (autoLogin) {
        const loginRes = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/user/login`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          credentials: "include",
          body: JSON.stringify({
            email: form.email,
            password: form.password,
          }),
        });

        if (!loginRes.ok) throw new Error(await loginRes.text());
        alert("자동 로그인 완료 ✅");
        router.replace("/");
        return;
      }

      router.push("/user/login");
    } catch (error) {
      console.error("❌ 회원가입 실패:", error);
      alert("회원가입 중 오류가 발생했습니다 ❌");
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <div className="bg-white shadow-md rounded-xl p-8 w-full max-w-md">
        <h1 className="text-2xl font-bold text-center text-blue-600 mb-6">
          회원가입
        </h1>

        <form className="space-y-4" onSubmit={handleSubmit}>
          {/* 이름 */}
          <div className="relative">
            <User className="absolute left-3 top-3 text-gray-400" size={18} />
            <input
              name="userName"
              placeholder="이름"
              value={form.userName}
              onChange={handleChange}
              className="w-full border rounded-md pl-10 pr-3 py-2 focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          {/* 비밀번호 */}
          <div className="relative">
            <Lock className="absolute left-3 top-3 text-gray-400" size={18} />
            <input
              type="password"
              name="password"
              placeholder="비밀번호 (8자 이상)"
              value={form.password}
              onChange={handleChange}
              className="w-full border rounded-md pl-10 pr-3 py-2 focus:ring-2 focus:ring-blue-500"
              minLength={8}
              required
            />
          </div>

          {/* 이메일 */}
          <div className="relative">
            <Mail className="absolute left-3 top-3 text-gray-400" size={18} />
            <input
              type="email"
              name="email"
              placeholder="이메일 주소"
              value={form.email}
              onChange={handleChange}
              className="w-full border rounded-md pl-10 pr-3 py-2 focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          {/* 자동 로그인 체크 */}
          <label className="flex items-center gap-2 text-sm select-none">
            <input
              type="checkbox"
              checked={autoLogin}
              onChange={(e) => setAutoLogin(e.target.checked)}
            />
            가입 후 자동 로그인
          </label>

          <button
            type="submit"
            className="w-full bg-blue-600 text-white py-2 rounded-md mt-4 hover:bg-blue-700 transition"
          >
            회원가입
          </button>
        </form>
      </div>
    </div>
  );
}
