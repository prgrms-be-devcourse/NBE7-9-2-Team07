"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Lock } from "lucide-react";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE_URL; // 예: http://localhost:8080

// ✅ [추가] 서버 응답 타입 정의 (RsData 구조를 명시적으로 지정)
type RsData = {
  errorCode: string;
  msg: string;
  data?: unknown;
};

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

      // ✅ API 호출 (백엔드 DELETE /api/user/delete)
      const res = await fetch(`${API_BASE}/api/user/delete`, {
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
        credentials: "include", // rq.getActor() 인증 유지
        body: JSON.stringify({ password }),
      });

      const contentType = res.headers.get("content-type") || "";

      // ✅ [수정됨] 'any' 대신 명시적 타입 사용 + 안전하게 초기화
      let rs: RsData | null = null;

      // ✅ [추가] 응답이 JSON일 경우만 파싱 (text/plain 대응)
      if (contentType.includes("application/json")) {
        rs = (await res.json()) as RsData; // 명시적 타입 캐스팅
      } else {
        console.error("서버 응답이 JSON이 아닙니다.");
      }

      // ✅ [수정됨] 타입 안정성 있게 검사
      if (!res.ok || rs?.errorCode !== "200") {
        alert(rs?.msg || "회원 탈퇴 중 오류가 발생했습니다.");
        return;
      }

      alert(rs.msg || "회원 탈퇴가 완료되었습니다.");

      // ✅ (선택) 탈퇴 후 홈 또는 별도 페이지로 이동
      router.replace("/");
    } catch (error) {
      // ✅ [추가] 에러 로그 추가
      console.error(error);
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
