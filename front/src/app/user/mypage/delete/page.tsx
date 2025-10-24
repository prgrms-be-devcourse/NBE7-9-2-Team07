"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Lock } from "lucide-react";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL; // 예: http://localhost:8080

export default function DeleteAccountPage() {
  const router = useRouter();
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // ✅ 입력값 검증
    if (!password) {
      alert("비밀번호를 입력해주세요.");
      return;
    }
    if (password.length < 8) {
      alert("비밀번호는 8자 이상이어야 합니다.");
      return;
    }

    // ✅ 최종 확인창
    if (!confirm("정말 탈퇴하시겠어요? 이 작업은 되돌릴 수 없습니다.")) {
      return;
    }

    try {
      setLoading(true);

      // ✅ API 호출 (컨트롤러의 @DeleteMapping("/delete") 와 연결)
      const res = await fetch(`${API_BASE}/api/user/delete`, {
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
        credentials: "include", // rq.getActor() 인증 유지
        body: JSON.stringify({ password }), // DeleteRequest.password 필드와 일치
      });

      const contentType = res.headers.get("content-type") || "";
      const body = contentType.includes("application/json")
        ? await res.json()
        : await res.text();
      const rs: any = typeof body === "string" ? null : body;

      if (!res.ok || rs?.errorCode !== "200") {
        alert(rs?.msg || "회원 탈퇴 중 오류가 발생했습니다.");
        return;
      }

      alert(rs.msg || "회원 탈퇴가 완료되었습니다.");

      // ✅ 서버에서 쿠키 삭제됨. 클라이언트 상태도 정리 필요 시:
      // localStorage.clear(); authContext.logout(); 등
      router.replace("/goodbye"); // 탈퇴 완료 페이지 혹은 홈으로 이동
    } catch (_) {
      alert("서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center bg-gray-50 px-4 h-[calc(100vh-64px)]">
      <div className="bg-white shadow-md rounded-xl p-8 w-full max-w-md">
        <h1 className="text-2xl font-bold text-center text-blue-600 mb-2">
          회원 탈퇴
        </h1>
        <h3 className="text-base text-center text-gray-400 mb-4">
          확인을 위해 비밀번호를 입력해주세요
        </h3>

        <form className="space-y-4" onSubmit={handleSubmit} noValidate>
          <div className="relative">
            <Lock className="absolute left-3 top-3 text-gray-400" size={18} />
            <input
              type="password"
              name="password"
              placeholder="비밀번호 (8자 이상)"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="w-full border rounded-md pl-10 pr-3 py-2 focus:ring-2 focus:ring-blue-500"
            />
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-blue-600 text-white py-2 rounded-md mt-4 hover:bg-blue-700 transition disabled:opacity-50"
          >
            {loading ? "탈퇴 처리 중..." : "회원 탈퇴"}
          </button>
        </form>
      </div>
    </div>
  );
}
