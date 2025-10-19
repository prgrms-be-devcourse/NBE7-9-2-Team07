import { fetchApi } from "@/lib/client";
import { BookmarkDto, LikeToggleResponse, PinDto, TagDto } from "../types/types";

// 태그
export const apiGetAllTags = () => fetchApi<TagDto[]>("/api/tags", { method: "GET" });
// 특정 핀의 태그
export const apiGetPinTags = (pinId: number) => fetchApi<TagDto[]>(`/api/pins/${pinId}/tags`, { method: "GET" });
// 핀에 태그 추가
export const apiAddTagToPin = (pinId: number, keyword: string) =>
  fetchApi(`/api/pins/${pinId}/tags`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ keyword }),
  });
// 핀에서 태그 삭제 (pinTagId가 아니라 tagId인 API로 제공됨)
export const apiRemoveTagFromPin = (pinId: number, tagId: number) =>
  fetchApi(`/api/pins/${pinId}/tags/${tagId}`, { method: "DELETE" });
// 태그 필터링 (쿼리 param: keywords=카페&keywords=감성)
export const apiFilterByTags = (keywords: string[]) => {
  const qs = keywords.map((k) => `keywords=${encodeURIComponent(k)}`).join("&");
  return fetchApi<PinDto[]>(`/api/tags/filter?${qs}`, { method: "GET" });
};

// 핀
export const apiCreatePin = (latitude: number, longitude: number, content: string) =>
  fetchApi<PinDto>("/api/pins", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ latitude, longitude, content }),
  });

export const apiGetPin = (id: number) => fetchApi<PinDto>(`/api/pins/${id}`, { method: "GET" });

// 반경 1km (서버가 lat/lng만 받으면 OK)
export const apiGetNearbyPins = (lat: number, lng: number) =>
  fetchApi<PinDto[]>(`/api/pins?latitude=${lat}&longitude=${lng}`, { method: "GET" });

// 전체 핀
export const apiGetAllPins = () => fetchApi<PinDto[]>("/api/pins/all", { method: "GET" });

// 핀 내용 수정
export const apiUpdatePin = (id: number, content: string) =>
  fetchApi<PinDto>(`/api/pins/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ content }),
  });

// 공개 여부 토글
export const apiTogglePublic = (id: number) => fetchApi<PinDto>(`/api/pins/${id}/public`, { method: "GET" });

// 핀 삭제
export const apiDeletePin = (id: number) => fetchApi<void>(`/api/pins/${id}`, { method: "DELETE" });

// 좋아요 토글
export const apiToggleLike = (pinId: number, userId: number) =>
  fetchApi<LikeToggleResponse>(`/api/pins/${pinId}/likes`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ userId }),
  });

// 좋아요 유저 목록
export const apiGetLikeUsers = (pinId: number) => fetchApi<{ id: number; userName: string }[]>(
  `/api/pins/${pinId}/likesusers`,
  { method: "GET" }
);

// 북마크
export const apiCreateBookmark = (userId: number, pinId: number) =>
  fetchApi<BookmarkDto>("/api/bookmarks", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ userId, pinId }),
  });

export const apiGetMyBookmarks = (userId: number) =>
  fetchApi<BookmarkDto[]>(`/api/bookmarks?userId=${userId}`, { method: "GET" });

export const apiDeleteBookmark = (bookmarkId: number, userId: number) =>
  fetchApi<void>(`/api/bookmarks/${bookmarkId}?userId=${userId}`, { method: "DELETE" });
