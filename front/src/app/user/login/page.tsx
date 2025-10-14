"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { User, Lock, Mail } from "lucide-react";

export default function LoginPage() {
  const router = useRouter();

  const [form, setForm] = useState({
    username: "",
    password: "",
  });
  const [error, setError] = useState("");

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: value });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setError("");

    // ✅ 빈칸 체크
    if (!form.username || !form.password) {
      setError("아이디와 비밀번호를 모두 입력해주세요.");
      return;
    }

    // ✅ 이메일 형식 검증 (아이디가 이메일일 수도 있으니까)
    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (form.username.includes("@") && !emailPattern.test(form.username)) {
      setError("이메일 형식이 올바르지 않습니다.");
      return;
    }

    // ✅ 가짜 유저 검증 (실제론 백엔드 API 호출)
    const mockUser = { username: "testuser", password: "12345678" };

    if (form.username !== mockUser.username) {
      setError("존재하지 않는 아이디입니다.");
      return;
    }
    if (form.password !== mockUser.password) {
      setError("비밀번호가 올바르지 않습니다.");
      return;
    }

    alert("로그인 성공 🎉");

    // ✅ 로그인 후 메인 페이지로 이동
    router.push("/home");
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <div className="bg-white shadow-md rounded-xl p-8 w-full max-w-md">
        <h1 className="text-2xl font-bold text-center text-blue-600 mb-6">로그인</h1>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* 아이디 / 이메일 */}
          <div className="relative">
            <User className="absolute left-3 top-3 text-gray-400" size={18} />
            <input
              name="username"
              placeholder="아이디 또는 이메일"
              value={form.username}
              onChange={handleChange}
              className="w-full border rounded-md pl-10 pr-3 py-2 focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* 비밀번호 */}
          <div className="relative">
            <Lock className="absolute left-3 top-3 text-gray-400" size={18} />
            <input
              type="password"
              name="password"
              placeholder="비밀번호"
              value={form.password}
              onChange={handleChange}
              className="w-full border rounded-md pl-10 pr-3 py-2 focus:ring-2 focus:ring-blue-500"
            />
          </div>

          {/* 에러 메시지 */}
          {error && <p className="text-red-500 text-sm text-center">{error}</p>}

          {/* 로그인 버튼 */}
          <button
            type="submit"
            className="w-full bg-blue-600 text-white py-2 rounded-md mt-4 hover:bg-blue-700 transition"
          >
            로그인
          </button>
        </form>

        <p className="text-sm text-gray-500 text-center mt-4">
          아직 회원이 아니신가요?{" "}
          <a href="/user/join" className="text-blue-600 hover:underline">
            회원가입
          </a>
        </p>
      </div>
    </div>
  );
}
