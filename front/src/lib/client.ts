// lib/client.ts
export type ApiResponse<T> = {
  code: string;
  message: string;
  data: T;
};

export async function fetchApi<T>(url: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}${url}`, {
    ...options,
    credentials: "include",
  });

  const rsData = await res.json();

  if (!res.ok) {
    throw new Error(rsData.message || "API 요청 실패");
  }

  // code/message 구조일 경우엔 data만, 없으면 전체 리턴
  return rsData.data ?? rsData;
}

