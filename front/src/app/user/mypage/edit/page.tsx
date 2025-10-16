"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Mail, Lock, User, AtSign } from "lucide-react";

export default function EditPage() {
  const router = useRouter();

  const [form, setForm] = useState({
    username: "",
    password: "",
    nickname: "",
    email: "",
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: value });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // ✅ 비밀번호 검증
    if (form.password.length < 8) {
      alert("비밀번호는 8자 이상이어야 합니다.");
      return;
    }

    // ✅ 이메일 형식 검증
    const emailOk = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.email);
    if (!emailOk) {
      alert("올바른 이메일 형식이 아닙니다.");
      return;
    }

    try {
      // ✅ 닉네임 중복 확인 요청
      const res = await fetch("/api/user/check-nickname", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ nickname: form.nickname }),
      });

      const data = await res.json();

      if (!res.ok || !data.available) {
        alert(data.message || "이미 존재하는 닉네임입니다.");
        return;
      }

      alert("회원 정보가 수정되었습니다 🎉");
      router.push("/user/mypage");
    } catch (err) {
      alert("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }
  };

  return (
    <div className="flex items-center justify-center bg-gray-50 px-4 h-[calc(100vh-64px)]">
      <div className="bg-white shadow-md rounded-xl p-8 w-full max-w-md">
        <h1 className="text-2xl font-bold text-center text-blue-600 mb-6">
          회원 정보 수정
        </h1>

        <form className="space-y-4" onSubmit={handleSubmit}>
          {/* 아이디 */}
          <div className="relative">
            <User className="absolute left-3 top-3 text-gray-400" size={18} />
            <input
              name="username"
              placeholder="아이디"
              value={form.username}
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

          {/* 닉네임 */}
          <div className="relative">
            <AtSign className="absolute left-3 top-3 text-gray-400" size={18} />
            <input
              name="nickname"
              placeholder="닉네임"
              value={form.nickname}
              onChange={handleChange}
              className="w-full border rounded-md pl-10 pr-3 py-2 focus:ring-2 focus:ring-blue-500"
              required
            />
          </div>

          {/* 이메일 */}
          <div className="relative flex gap-2 items-center">
            <div className="relative flex-1">
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
          </div>

          <button
            type="submit"
            className="w-full bg-blue-600 text-white py-2 rounded-md mt-4 hover:bg-blue-700 transition"
          >
            수정 완료
          </button>
        </form>
      </div>
    </div>
  );
}
