"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Mail, Lock, User, AtSign, CheckCircle2 } from "lucide-react";

export default function SignUpPage() {
  const router = useRouter();

  const [form, setForm] = useState({
    userName: "", // ✅ 백엔드에서 요구하는 필드명
    password: "",
    email: "",
  });
  const [emailVerified, setEmailVerified] = useState(false);
  const [nickname, setNickname] = useState(""); // 백엔드에선 안 쓰지만, UI는 유지

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setForm({ ...form, [name]: value });
  };

  // ✅ 회원가입 요청
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!emailVerified) {
      alert("이메일 인증을 완료해주세요.");
      return;
    }

    try {
      const res = await fetch("http://localhost:8080/api/user/join", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email: form.email,
          password: form.password,
          userName: form.userName, // 백엔드 요구 필드명과 정확히 일치
        }),
      });

      if (!res.ok) {
        const err = await res.text();
        throw new Error(err);
      }

      const data = await res.json();
      console.log("✅ 회원가입 성공:", data);

      alert("회원가입이 완료되었습니다 🎉");
      router.push("/user/login");
    } catch (error) {
      console.error("❌ 회원가입 실패:", error);
      alert("회원가입 중 오류가 발생했습니다 ❌");
    }
  };

  const handleEmailVerify = () => {
    alert("인증 메일이 발송되었습니다 📧");
    setTimeout(() => {
      setEmailVerified(true);
      alert("이메일 인증이 완료되었습니다 ✅");
    }, 1500);
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
      <div className="bg-white shadow-md rounded-xl p-8 w-full max-w-md">
        <h1 className="text-2xl font-bold text-center text-blue-600 mb-6">회원가입</h1>

        <form className="space-y-4" onSubmit={handleSubmit}>
          {/* 이름(userName) */}
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
            <button
              type="button"
              onClick={handleEmailVerify}
              className={`text-sm px-3 py-2 rounded-md ${
                emailVerified
                  ? "bg-green-500 text-white"
                  : "bg-blue-600 text-white hover:bg-blue-700"
              }`}
            >
              {emailVerified ? "인증 완료" : "인증 요청"}
            </button>
          </div>

          {emailVerified && (
            <div className="flex items-center gap-2 text-green-600 text-sm mt-1">
              <CheckCircle2 size={16} /> 이메일 인증이 완료되었습니다.
            </div>
          )}

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
