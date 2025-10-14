export type RsData<T> = {
  resultCode: string;
  msg: string;
  data: T;
};

export async function fetchApi<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}${url}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(options?.headers || {}),
    },
    credentials: "include", // 세션 쿠키 유지
  });

  // JSON 파싱 시 실패 가능성 대비
  let rsData: RsData<T>;
  try {
    rsData = await res.json();
  } catch {
    throw new Error("⚠️ 서버로부터 올바른 JSON 응답을 받지 못했습니다.");
  }

  // ✅ HTTP 상태 코드 확인
  if (!res.ok) {
    throw new Error(rsData?.msg || "API 요청 실패");
  }

  // ✅ 백엔드 resultCode 확인 (예: 200-OK, 400-BAD)
  if (!rsData.resultCode?.startsWith("200")) {
    throw new Error(rsData.msg || "서버 처리 실패");
  }

  // ✅ resultCode/msg 구조면 data만 반환, 그 외엔 전체 반환
  return rsData.data ?? (rsData as unknown as T);
}
