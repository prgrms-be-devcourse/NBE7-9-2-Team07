// lib/client.ts
export type ApiResponse<T> = {
  code: string;
  message: string;
  data: T;
};

export async function fetchApi<T>(url: string, options?: RequestInit): Promise<ApiResponse<T>> {
  if (options?.body) {
    const headers = new Headers(options.headers || {});
    headers.set("Content-Type", "application/json");
    options.headers = headers;
  }

  const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}${url}`, {
    ...options,
    credentials: "include", // 세션 쿠키 전송
  });

  const rsData = await res.json();

  if (!res.ok || rsData.code !== "200") {
    throw new Error(rsData.message || "API 요청 실패");
  }

  return rsData;
}
