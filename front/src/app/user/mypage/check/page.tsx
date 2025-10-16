"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Lock } from "lucide-react";

export default function CheckPage() {
  const router = useRouter();
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  // 서버로 비밀번호 검증 요청
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (password.length < 8) {
      alert("비밀번호는 8자 이상이어야 합니다.");
      return;
    }

    try {
      setLoading(true);

      const res = await fetch("/api/auth/check-password", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ password }),
        credentials: "include", // 쿠키/세션 로그인 유지
      });

      const data = await res.json();

      if (!res.ok || !data.success) {
        alert(data.message || "비밀번호가 일치하지 않습니다.");
        return;
      }

      alert("비밀번호 확인 완료 🎉");
      router.push("/user/mypage/edit"); // 수정 페이지로 이동

    } catch (err) {
      alert("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center bg-gray-50 px-4 h-[calc(100vh-64px)]">
      <div className="bg-white shadow-md rounded-xl p-8 w-full max-w-md">
        <h1 className="text-2xl font-bold text-center text-blue-600 mb-2">
          회원 정보 수정
        </h1>
        <h3 className="text-base text-center text-gray-400 mb-4">
          확인을 위해 비밀번호를 입력해주세요
        </h3>

        <form className="space-y-4" onSubmit={handleSubmit}>
          {/* 비밀번호 입력 */}
          <div className="relative">
            <Lock className="absolute left-3 top-3 text-gray-400" size={18} />
            <input
              type="password"
              name="password"
              placeholder="비밀번호 (8자 이상)"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full border rounded-md pl-10 pr-3 py-2 focus:ring-2 focus:ring-blue-500"
              minLength={8}
              required
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 text-white py-2 rounded-md mt-4 hover:bg-blue-700 transition disabled:opacity-50"
          >
            {loading ? "확인 중..." : "입력 완료"}
          </button>
        </form>
      </div>
    </div>
  );
}
