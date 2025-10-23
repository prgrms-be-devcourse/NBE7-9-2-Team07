import { fetchApi } from "@/lib/client";
import {
  BookmarkDto,
  GetFilteredPinResponse,
  LikesStatusDto,
  PinDto,
  PinLikedUserDto,
  TagDto,
} from "../types/types";

// ---------- Tags ----------
export const apiGetAllTags = async (): Promise<TagDto[]> => {
  const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/tags`);
  const json = await res.json();

  // ✅ 정확한 구조 대응: { data: { tags: [...] } }
  if (Array.isArray(json?.data?.tags)) {
    return json.data.tags;
  }

  // ✅ fallback (혹시 서버 구조가 바뀌더라도 대응)
  if (Array.isArray(json?.data)) {
    return json.data;
  }

  return [];
};

export const apiGetPinTags = async (pinId: number): Promise<TagDto[]> => {
  const res = await fetch(
    `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/pins/${pinId}/tags`
  );
  const data = await res.json();

  // ✅ 새 구조 대응
  if (Array.isArray(data?.data?.tags)) {
    return data.data.tags;
  }

  // ✅ 예전 구조 fallback
  if (Array.isArray(data?.data)) {
    return data.data;
  }

  return [];
};

export const apiAddTagToPin = (pinId: number, keyword: string) =>
  fetchApi(`/api/pins/${pinId}/tags`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ keyword }),
  });

export const apiRemoveTagFromPin = (pinId: number, tagId: number) =>
  fetchApi(`/api/pins/${pinId}/tags/${tagId}`, { method: "DELETE" });

export const apiRestoreTagOnPin = (pinId: number, tagId: number) =>
  fetchApi(`/api/pins/${pinId}/tags/${tagId}/restore`, { method: "PATCH" });

export const apiFilterByTags = (keywords: string[]) => {
  const qs = keywords.map((k) => `keywords=${encodeURIComponent(k)}`).join("&");
  return fetchApi<GetFilteredPinResponse[]>(`/api/tags/filter?${qs}`, {
    method: "GET",
  });
};

// ---------- Pins ----------
// pincoApi.ts
export const apiCreatePin = async (
  latitude: number,
  longitude: number,
  content: string
): Promise<PinDto> => {
  console.log("📤 보내는 요청:", { latitude, longitude, content });

  const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/pins`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      latitude: Number(latitude), // ✅ 숫자형으로 강제
      longitude: Number(longitude),
      content: content.trim(), // ✅ 공백 제거
    }),
    credentials: "include", // ✅ (수정) 인증 쿠키 포함
  });

  const json = await res.json();
  console.log("🧭 좌표값 확인:", latitude, longitude);
  console.log("📥 서버 응답:", json);

  if (json?.data) return json.data as PinDto;
  throw new Error("핀 생성 실패: 서버 응답에 data가 없습니다");
};

export const apiGetPin = (id: number) =>
  fetchApi<PinDto>(`/api/pins/${id}`, { method: "GET" });

export const apiGetNearbyPins = (lat: number, lng: number) =>
  fetchApi<PinDto[] | null>(`/api/pins?latitude=${lat}&longitude=${lng}`, {
    method: "GET",
  });

export const apiGetAllPins = () =>
  fetchApi<PinDto[] | null>("/api/pins/all", { method: "GET" });

export const apiUpdatePin = async (
  id: number,
  latitude: number,
  longitude: number,
  content: string
): Promise<PinDto> => {
  // ✅ (수정) fetchApi 사용
  const updatedPin = await fetchApi<PinDto>(`/api/pins/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ latitude, longitude, content }),
  });

  // ✅ RsData 구조에 대응 (data 필드 추출) - fetchApi가 data를 추출해줌
  if (updatedPin) {
    return updatedPin;
  } else {
    throw new Error("핀 수정 실패: 서버 응답에 data가 없습니다");
  }
};

// 컨트롤러가 PUT 으로 공개 토글
export const apiTogglePublic = (id: number) =>
  fetchApi<PinDto>(`/api/pins/${id}/public`, { method: "PUT" });

export const apiDeletePin = (id: number) =>
  fetchApi<void>(`/api/pins/${id}`, { method: "DELETE" });

// ---------- Likes ----------

// ✅ 좋아요 추가
export const apiAddLike = async (pinId: number, userId: number) => {
  const res = await fetch(
    `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/pins/${pinId}/likes`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId }),
      credentials: "include", // ✅ (수정) 인증 쿠키 포함
    }
  );
  return await res.json(); // ✅ { data: { isLiked, likeCount } }
};

// ✅ 좋아요 취소
export const apiRemoveLike = async (pinId: number, userId: number) => {
  const res = await fetch(
    `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/pins/${pinId}/likes`,
    {
      method: "DELETE",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ userId }),
      credentials: "include", // ✅ (수정) 인증 쿠키 포함
    }
  );
  return await res.json(); // ✅ { data: { isLiked, likeCount } }
};

export const apiGetLikeUsers = (pinId: number) =>
  fetchApi<PinLikedUserDto[]>(`/api/pins/${pinId}/likesusers`, {
    method: "GET",
  });

// ---------- Bookmarks ----------

// ✅ (수정) apiCreateBookmark 함수
export const apiCreateBookmark = (pinId: number) => {
  // ❌ "/api/bookmarks"가 아니라 아래 주소로 수정
  return fetchApi<BookmarkDto>(`/api/pins/${pinId}/bookmarks`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ pinId }), // 👈 이 body는 PinController가 받는 addBookmarkRequest와 일치
  });
};

// ✅ (수정) apiGetMyBookmarks 함수 (userId 제거)
export const apiGetMyBookmarks = () => {
  return fetchApi<BookmarkDto[] | null>(`/api/bookmarks`, { method: "GET" });
};

// ✅ (수정) apiDeleteBookmark 함수 (userId 제거)
export const apiDeleteBookmark = (bookmarkId: number) => {
  return fetchApi<void>(`/api/bookmarks/${bookmarkId}`, { method: "DELETE" });
};