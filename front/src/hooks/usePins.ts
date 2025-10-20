"use client";

import { useEffect, useState } from "react";

export interface PinDto {
  id: number;
  latitude: number;
  longitude: number;
  content: string;
  userId: number;
  likeCount: number;
  isPublic: boolean;
  createdAt: string;
  modifiedAt: string;
}

export interface TagDto {
  id: number;
  keyword: string;
  createdAt: string;
}

export type Mode = "all" | "nearby" | "tag" | "bookmark" | "liked";

interface UsePinsProps {
  lat: number;
  lng: number;
}

export function usePins(initialCenter: UsePinsProps, userId?: number) {
  const [pins, setPins] = useState<PinDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [mode, setMode] = useState<Mode>("all");
  const [center, setCenter] = useState(initialCenter);
  const [selectedPin, setSelectedPin] = useState<PinDto | null>(null);
  const [selectedTags, setSelectedTags] = useState<string[]>([]);
  const [allTags, setAllTags] = useState<TagDto[]>([]);

  /** ✅ 공통 정규화 함수 */
  const normalizePins = (arr: any[] | null | undefined): PinDto[] => {
    if (!Array.isArray(arr)) return [];
    return arr.map((p) => ({
      ...p,
      likeCount:
        typeof p.likeCount === "number"
          ? p.likeCount
          : p.likeCount != null
            ? Number(p.likeCount) || 0
            : 0,
      isPublic:
        typeof p.isPublic === "boolean"
          ? p.isPublic
          : p.isPublic == null
            ? true
            : Boolean(p.isPublic),
    }));
  };

  /** ✅ 모든 태그 자동 로드 */
  useEffect(() => {
    const fetchTags = async () => {
      try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/tags`);
        const data = await res.json();
        if (data.errorCode === "200") setAllTags(data.data);
        else setAllTags([]);
      } catch (e) {
        console.error("태그 목록 로드 실패", e);
        setAllTags([]);
      }
    };
    fetchTags();
  }, []);

  /** ✅ 모든 핀 조회 */
  const loadAllPins = async () => {
    setLoading(true);
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/pins/all`);
      const data = await res.json();
      console.log("📡 전체 핀 응답", data);
      console.log("📡 정규화 후", normalizePins(data?.data));
      setPins(normalizePins(data?.data));
      setMode("all");
    } catch (e) {
      console.error("전체 핀 로드 실패", e);
      setPins([]);
    } finally {
      setLoading(false);
    }
  };

  // ✅ 페이지 첫 렌더링 시 자동 전체 핀 로드
  useEffect(() => {
    loadAllPins();
  }, []);

  /** ✅ 주변 핀 조회 */
  const loadNearbyPins = async (lat?: number, lng?: number) => {
    setLoading(true);
    try {
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/pins?latitude=${lat ?? center.lat}&longitude=${lng ?? center.lng}`
      );
      const data = await res.json();
      setPins(normalizePins(data?.data));
      setMode("nearby");
    } catch (e) {
      console.error("주변 핀 로드 실패", e);
      setPins([]);
    } finally {
      setLoading(false);
    }
  };

  /** ✅ 태그 기반 필터링 */
  const applyTagFilter = async (tags: string[]) => {
    setSelectedTags(tags);
    if (tags.length === 0) return clearTagFilter();

    setLoading(true);
    try {
      const query = tags.map((t) => `keywords=${encodeURIComponent(t)}`).join("&");
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/tags/filter?${query}`);
      const data = await res.json();
      setPins(normalizePins(data?.data));
      setMode("tag");
    } catch (e) {
      console.error("태그 필터 실패", e);
      setPins([]);
    } finally {
      setLoading(false);
    }
  };

  /** ✅ 태그 전체 해제 + 전체 보기로 전환 */
  const clearTagFilter = async () => {
    setSelectedTags([]);
    setMode("all");
    await loadAllPins();
  };

  /** ✅ 내 북마크 핀 로드 */
  const loadMyBookmarks = async () => {
    if (!userId) return;
    setLoading(true);
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/bookmarks?userId=${userId}`);
      const data = await res.json();
      const pinsOnly = Array.isArray(data?.data) ? data.data.map((b: any) => b.pin) : [];
      setPins(normalizePins(pinsOnly));
      setMode("bookmark");
    } catch (e) {
      console.error("북마크 핀 로드 실패", e);
      setPins([]);
    } finally {
      setLoading(false);
    }
  };

  /** ✅ 내가 좋아요한 핀 로드 */
  const loadLikedPins = async () => {
    if (!userId) return;
    setLoading(true);
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/user/${userId}/likespins`);
      const data = await res.json();
      setPins(normalizePins(data?.data));
      setMode("liked");
    } catch (e) {
      console.error("좋아요 핀 로드 실패", e);
      setPins([]);
    } finally {
      setLoading(false);
    }
  };

  /** ✅ 핀 클릭 시 태그 로드 */
  const ensurePinTagsLoaded = async (pin: PinDto) => {
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/pins/${pin.id}/tags`);
      const data = await res.json();
      if (data.errorCode === "200" && Array.isArray(data.data)) {
        return { ...pin, tags: data.data.map((t: any) => t.keyword) as string[] };
      }
    } catch (e) {
      console.error("핀 태그 로드 실패", e);
    }
    return { ...pin, tags: [] as string[] };
  };

  return {
    pins,
    loading,
    mode,
    center,
    selectedPin,
    selectedTags,
    allTags,
    setCenter,
    setSelectedPin,
    loadAllPins,
    loadNearbyPins,
    applyTagFilter,
    clearTagFilter,
    loadMyBookmarks,
    loadLikedPins,
    ensurePinTagsLoaded,
  };
}
