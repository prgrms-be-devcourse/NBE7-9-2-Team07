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
export const apiGetAllTags = () =>
  fetchApi<TagDto[]>("/api/tags", { method: "GET" });

export const apiGetPinTags = (pinId: number) =>
  fetchApi<TagDto[]>(`/api/pins/${pinId}/tags`, { method: "GET" });

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
  const qs = keywords.map(k => `keywords=${encodeURIComponent(k)}`).join("&");
  return fetchApi<GetFilteredPinResponse[]>(`/api/tags/filter?${qs}`, { method: "GET" });
};

// ---------- Pins ----------
export const apiCreatePin = (latitude: number, longitude: number, content: string) =>
  fetchApi<PinDto>("/api/pins", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ latitude, longitude, content }),
  });

export const apiGetPin = (id: number) =>
  fetchApi<PinDto>(`/api/pins/${id}`, { method: "GET" });

export const apiGetNearbyPins = (lat: number, lng: number) =>
  fetchApi<PinDto[] | null>(`/api/pins?latitude=${lat}&longitude=${lng}`, { method: "GET" });

export const apiGetAllPins = () =>
  fetchApi<PinDto[] | null>("/api/pins/all", { method: "GET" });

export const apiUpdatePin = (id: number, latitude: number, longitude: number, content: string) =>
  fetchApi<PinDto>(`/api/pins/${id}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ latitude, longitude, content }),
  });

// 컨트롤러가 PUT 으로 공개 토글
export const apiTogglePublic = (id: number) =>
  fetchApi<PinDto>(`/api/pins/${id}/public`, { method: "PUT" });

export const apiDeletePin = (id: number) =>
  fetchApi<void>(`/api/pins/${id}`, { method: "DELETE" });

// ---------- Likes ----------
export const apiToggleLike = (pinId: number, userId: number) =>
  fetchApi<LikesStatusDto>(`/api/pins/${pinId}/likes`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ userId }),
  });

export const apiGetLikeUsers = (pinId: number) =>
  fetchApi<PinLikedUserDto[]>(`/api/pins/${pinId}/likesusers`, { method: "GET" });

// ---------- Bookmarks ----------
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
