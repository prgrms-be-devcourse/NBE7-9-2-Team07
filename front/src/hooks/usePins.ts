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

  /** ✅ 모든 태그 자동 로드 */
  useEffect(() => {
    const fetchTags = async () => {
      try {
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/tags`);
        const data = await res.json();
        if (data.errorCode === "200") {
          setAllTags(data.data);
        } else {
          setAllTags([]);
        }
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
      if (data.errorCode === "200") setPins(data.data);
      else setPins([]);
      setMode("all");
    } catch (e) {
      console.error("전체 핀 로드 실패", e);
      setPins([]);
    } finally {
      setLoading(false);
    }
  };

  /** ✅ 주변 핀 조회 */
  const loadNearbyPins = async (lat?: number, lng?: number) => {
    setLoading(true);
    try {
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/pins?latitude=${lat ?? center.lat}&longitude=${lng ?? center.lng}`
      );
      const data = await res.json();
      if (data.errorCode === "200") setPins(data.data);
      else setPins([]);
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
    // 선택 상태를 먼저 반영해서 버튼 하이라이트가 즉시 바뀌도록 함
    setSelectedTags(tags);
    if (tags.length === 0) {
      return clearTagFilter(); // 빈 배열이면 전체 해제 처리로 연결
    }

    setLoading(true);
    try {
      const query = tags.map((t) => `keywords=${encodeURIComponent(t)}`).join("&");
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/tags/filter?${query}`);
      const data = await res.json();
      if (data.errorCode === "200") setPins(data.data);
      else setPins([]);
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
    setSelectedTags([]);      // 버튼 하이라이트 즉시 해제
    setMode("all");           // 상단 필터 버튼 상태도 전체보기로
    await loadAllPins();      // 리스트/지도도 전체로 갱신
  };

  /** ✅ 내 북마크 핀 로드 */
  const loadMyBookmarks = async () => {
    if (!userId) return;
    setLoading(true);
    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/bookmarks?userId=${userId}`);
      const data = await res.json();
      if (data.errorCode === "200") {
        const pinsOnly = data.data.map((b: any) => b.pin);
        setPins(pinsOnly);
      } else {
        setPins([]);
      }
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
      if (data.errorCode === "200") setPins(data.data);
      else setPins([]);
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

    // 노출 함수들
    loadAllPins,
    loadNearbyPins,
    applyTagFilter,
    clearTagFilter,     // 👈 추가
    loadMyBookmarks,
    loadLikedPins,
    ensurePinTagsLoaded,
  };
}
