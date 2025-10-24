"use client";
import { useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/context/AuthContext";

export default function LoginPage() {
  const router = useRouter();
  const { login } = useAuth();

  const [email, setEmail] = useState("user1@example.com");       // 기본값 X
  const [password, setPassword] = useState("12345678"); // 기본값 X
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (submitting) return;
    setSubmitting(true);

    const ok = await login(email.trim(), password.trim());

    setSubmitting(false);

    if (ok) {
      alert("로그인 성공 🎉");
      // 필요에 맞게 이동 경로만 바꿔줘 (/home 사용 중이면 그대로 두면 됨)
      router.push("/");
    } else {
      alert("로그인 실패 ❌");
    }
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="flex flex-col gap-3 w-80 mx-auto mt-40"
    >
      <input
        type="email"
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="이메일"
        className="border rounded p-2"
        autoComplete="email"
        required
      />
      <input
        type="password"
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="비밀번호"
        className="border rounded p-2"
        autoComplete="current-password"
        required
        minLength={8}
      />
      <button
        type="submit"
        className="bg-blue-600 text-white rounded p-2"
        disabled={submitting || !email || !password}
      >
        {submitting ? "로그인 중..." : "로그인"}
      </button>

      <div className="text-sm text-gray-500 text-center mt-2">
        <p>기존 계정으로 로그인해주세요</p>
      </div>
    </form>
  );
}
