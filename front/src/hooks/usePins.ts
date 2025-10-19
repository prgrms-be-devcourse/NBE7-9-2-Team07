import { useEffect, useMemo, useState } from "react";
import { PinDto, TagDto } from "../types/types";
import {
  apiFilterByTags,
  apiGetAllPins,
  apiGetAllTags,
  apiGetMyBookmarks,
  apiGetNearbyPins,
  apiGetPinTags,
} from "../lib/pincoApi";

type Mode = "all" | "nearby" | "tag" | "bookmark";

export function usePins(initialCenter = { lat: 37.5665, lng: 126.978 }) {
  const [allPins, setAllPins] = useState<PinDto[]>([]);
  const [displayPins, setDisplayPins] = useState<PinDto[]>([]);
  const [mode, setMode] = useState<Mode>("all");
  const [loading, setLoading] = useState(false);

  // 검색/필터
  const [search, setSearch] = useState("");
  const [selectedTags, setSelectedTags] = useState<string[]>([]);
  const [allTags, setAllTags] = useState<TagDto[]>([]);

  // 위치
  const [center, setCenter] = useState<{ lat: number; lng: number }>(initialCenter);

  // 선택
  const [selectedPin, setSelectedPin] = useState<PinDto | null>(null);

  // 북마크용 (로그인 유저 아이디가 있으면 사용)
  const [userIdForBookmark, setUserIdForBookmark] = useState<number | null>(1); // TODO: useAuth에서 가져오면 교체

  // 초기 태그 전체
  useEffect(() => {
    (async () => {
      try {
        const tags = await apiGetAllTags();
        setAllTags(tags);
      } catch (e) {
        console.error("태그 목록 조회 실패", e);
      }
    })();
  }, []);

  // 전체 핀
  const loadAllPins = async () => {
    setLoading(true);
    try {
      const pins = await apiGetAllPins();
      setAllPins(pins);
      setMode("all");
    } catch (e) {
      console.error("전체 핀 조회 실패", e);
    } finally {
      setLoading(false);
    }
  };

  // 주변 1km
  const loadNearbyPins = async (lat?: number, lng?: number) => {
    setLoading(true);
    try {
      const targetLat = lat ?? center.lat;
      const targetLng = lng ?? center.lng;
      const pins = await apiGetNearbyPins(targetLat, targetLng);
      setAllPins(pins);
      setMode("nearby");
      setCenter({ lat: targetLat, lng: targetLng });
    } catch (e) {
      console.error("주변 핀 조회 실패", e);
    } finally {
      setLoading(false);
    }
  };

  // 태그 필터 (서버 사이드 필터 활용)
  const applyTagFilter = async (tags: string[]) => {
    setSelectedTags(tags);
    if (tags.length === 0) {
      // 태그 해제 → 전체 모드 유지
      setMode("all");
      setAllPins(await apiGetAllPins());
      return;
    }
    setLoading(true);
    try {
      const pins = await apiFilterByTags(tags);
      setAllPins(pins);
      setMode("tag");
    } catch (e) {
      console.error("태그 필터 실패", e);
    } finally {
      setLoading(false);
    }
  };

  // 내 북마크
  const loadMyBookmarks = async () => {
    if (!userIdForBookmark) return;
    setLoading(true);
    try {
      const bookmarks = await apiGetMyBookmarks(userIdForBookmark);
      const pins = bookmarks.map((b) => b.pin);
      setAllPins(pins);
      setMode("bookmark");
    } catch (e) {
      console.error("북마크 조회 실패", e);
    } finally {
      setLoading(false);
    }
  };

  // 리스트 검색 필터 (클라이언트 사이드)
  const filtered = useMemo(() => {
    if (!search.trim()) return allPins;
    const q = search.toLowerCase();
    return allPins.filter((p) => p.content.toLowerCase().includes(q));
  }, [allPins, search]);

  useEffect(() => {
    setDisplayPins(filtered);
  }, [filtered]);

  // 초기 로드: 전체 핀
  useEffect(() => {
    loadAllPins();
  }, []);

  // 카드/마커에 태그가 필요할 때 on-demand 로딩
  const ensurePinTagsLoaded = async (pin: PinDto) => {
    if (pin._tagsLoaded) return pin;
    try {
      const tags = await apiGetPinTags(pin.id);
      const next: PinDto = {
        ...pin,
        tags: tags.map((t) => t.keyword),
        _tagsLoaded: true,
      };
      // allPins, displayPins 둘 다 업데이트
      setAllPins((prev) => prev.map((p) => (p.id === pin.id ? next : p)));
      setDisplayPins((prev) => prev.map((p) => (p.id === pin.id ? next : p)));
      return next;
    } catch (e) {
      console.error("핀 태그 로딩 실패", e);
      return pin;
    }
  };

  return {
    // 상태
    pins: displayPins,
    loading,
    mode,
    center,
    selectedPin,
    selectedTags,
    allTags,

    // 상태 set
    setSearch,
    setCenter,
    setSelectedPin,

    // 동작
    loadAllPins,
    loadNearbyPins,
    applyTagFilter,
    loadMyBookmarks,
    ensurePinTagsLoaded,

    // 유저아이디 설정(북마크용)
    setUserIdForBookmark,
  };
}
