"use client";

import {useEffect, useState} from "react";

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
    tags?: string[];
}

export interface TagDto {
    id: number;
    keyword: string;
    createdAt: string;
}

export type Mode = "screen" | "nearby" | "tag" | "bookmark" | "liked";

interface UsePinsProps {
    lat: number;
    lng: number;
}

export function usePins(initialCenter: UsePinsProps, userId?: number | null) {
    const [pins, setPins] = useState<PinDto[]>([]);
    const [allLoadedPins, setAllLoadedPins] = useState<PinDto[]>([]);
    const [loading, setLoading] = useState(false);
    const [mode, setMode] = useState<Mode>("nearby");
    const [center, setCenter] = useState(initialCenter);
    const [selectedPin, setSelectedPin] = useState<PinDto | null>(null);
    const [selectedTags, setSelectedTags] = useState<string[]>([]);
    const [allTags, setAllTags] = useState<TagDto[]>([]);

    /* =========================================================
       ✅ 공통 유틸 함수
    ========================================================= */

    const extractArray = (data: any): any[] => {
        if (!data) return [];
        if (Array.isArray(data)) return data;
        if (Array.isArray(data?.pins)) return data.pins;
        if (Array.isArray(data?.tags)) return data.tags;
        return [];
    };

    const normalizePins = (arr: any[] | null | undefined): PinDto[] => {
        if (!Array.isArray(arr)) return [];
        return arr.map((p, idx) => {
            const pin = p.pin ?? p;
            return {
                id: pin.id ?? idx + Math.random(),
                latitude: Number(pin.latitude) || 0,
                longitude: Number(pin.longitude) || 0,
                content: pin.content ?? "",
                userId: pin.userId ?? 0,
                likeCount:
                    typeof pin.likeCount === "number"
                        ? pin.likeCount
                        : Number(pin.likeCount) || 0,
                isPublic: Boolean(pin.isPublic ?? true),
                createdAt: pin.createdAt ?? "",
                modifiedAt: pin.modifiedAt ?? "",
                tags: pin.tags ?? [],
            };
        });
    };

    const filterPinsByTags = (pinsToFilter: PinDto[], tags: string[]): PinDto[] => {
        if (tags.length === 0) return pinsToFilter;

        return pinsToFilter.filter(pin => {
            if (!pin.tags || pin.tags.length === 0) return false;
            return tags.every(tag => pin.tags?.includes(tag));
        });
    };

    /* =========================================================
       ✅ 태그 목록 로드
    ========================================================= */
    const fetchTags = async () => {
        try {
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/tags`);
            const data = await res.json();

            const tagsArray = extractArray(data.data);
            setAllTags(tagsArray);
        } catch (e) {
            console.error("태그 목록 로드 실패:", e);
            setAllTags([]);
        }
    };

    useEffect(() => {
        fetchTags();
    }, []);

    const reloadTags = async () => {
        await fetchTags();
    };

    /* =========================================================
       ✅ 화면상 모든 핀 조회
    ========================================================= */
    const loadAllPins = async (lat?: number, lng?: number, radius?: number) => {
        setLoading(true);
        try {
            const apiKey = localStorage.getItem("apiKey");
            const accessToken = localStorage.getItem("accessToken");

            const headers: HeadersInit = {
                "Content-Type": "application/json",
            };

            if (apiKey && accessToken) {
                headers["Authorization"] = `Bearer ${apiKey} ${accessToken}`;
            }

            const validRadius = radius && radius > 0 ? radius : undefined;
            const radiusParam = validRadius ? `&radius=${validRadius}` : '';

            const url = `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/pins?latitude=${lat ?? center.lat}&longitude=${lng ?? center.lng}${radiusParam}`;

            const res = await fetch(url, {
                method: "GET",
                headers: headers,
                credentials: "include",
            });
            const data = await res.json();

            const pinArray = extractArray(data.data);
            const normalized = normalizePins(pinArray);

            const pinsWithTags = await loadTagsForPins(normalized);
            setAllLoadedPins(pinsWithTags);

            const filtered = filterPinsByTags(pinsWithTags, selectedTags);
            setPins(filtered);

            setMode("screen");
        } catch (e) {
            console.error("화면 전체 핀 로드 실패:", e);
            setPins([]);
            setAllLoadedPins([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadAllPins();
    }, []);

    /* =========================================================
       ✅ 주변 핀 조회
    ========================================================= */
    const loadNearbyPins = async (lat?: number, lng?: number) => {
        setLoading(true);
        try {
            const apiKey = localStorage.getItem("apiKey");
            const accessToken = localStorage.getItem("accessToken");

            const headers: HeadersInit = {
                "Content-Type": "application/json",
            };

            if (apiKey && accessToken) {
                headers["Authorization"] = `Bearer ${apiKey} ${accessToken}`;
            }

            const url = `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/pins?latitude=${lat ?? center.lat}&longitude=${lng ?? center.lng}`;

            const res = await fetch(url, {
                method: "GET",
                headers: headers,
                credentials: "include",
            });
            const data = await res.json();

            const pinArray = extractArray(data.data);
            const normalized = normalizePins(pinArray);

            const pinsWithTags = await loadTagsForPins(normalized);
            setPins(pinsWithTags);
            setAllLoadedPins(pinsWithTags);
            setMode("nearby");

            // ✅ 모드 변경 시 필터 초기화
            setSelectedTags([]);
        } catch (e) {
            console.error("주변 핀 로드 실패:", e);
            setPins([]);
            setAllLoadedPins([]);
        } finally {
            setLoading(false);
        }
    };

    /* =========================================================
       ✅ 태그 기반 필터링 (모드 유지)
    ========================================================= */
    const applyTagFilter = async (tags: string[]) => {
        setSelectedTags(tags);

        // ✅ 태그 전체 해제 시
        if (tags.length === 0) {
            // screen, bookmark, liked 모드일 때는 전체 로드된 핀 복원
            if (mode === "screen" || mode === "bookmark" || mode === "liked") {
                setPins(allLoadedPins);
                return;
            }
            // nearby나 tag 모드일 때는 주변 핀 다시 로드
            await loadNearbyPins();
            return;
        }

        // ✅ screen 모드: 클라이언트 사이드 필터링
        if (mode === "screen") {
            const filtered = filterPinsByTags(allLoadedPins, tags);
            setPins(filtered);
            return;
        }

        // ✅ bookmark 모드: 클라이언트 사이드 필터링
        if (mode === "bookmark") {
            const filtered = filterPinsByTags(allLoadedPins, tags);
            setPins(filtered);
            return;
        }

        // ✅ liked 모드: 클라이언트 사이드 필터링
        if (mode === "liked") {
            const filtered = filterPinsByTags(allLoadedPins, tags);
            setPins(filtered);
            return;
        }

        // ✅ nearby 또는 tag 모드: 서버 사이드 필터링
        setLoading(true);
        try {
            const query = tags.map((t) => `keywords=${encodeURIComponent(t)}`).join("&");
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/tags/filter?${query}`);
            const data = await res.json();

            const filteredPins = extractArray(data.data);
            const normalized = normalizePins(filteredPins);

            const pinsWithTags = await loadTagsForPins(normalized);
            setPins(pinsWithTags);
            setAllLoadedPins(pinsWithTags);
            setMode("tag");
        } catch (e) {
            console.error("태그 필터 실패:", e);
            setPins([]);
            setAllLoadedPins([]);
        } finally {
            setLoading(false);
        }
    };

    /* =========================================================
       ✅ 태그 전체 해제 (deprecated - applyTagFilter([])를 사용)
    ========================================================= */
    const clearTagFilter = async () => {
        await applyTagFilter([]);
    };

    /* =========================================================
       ✅ 북마크 핀 로드
    ========================================================= */
    const loadMyBookmarks = async () => {
        console.log("🔖 loadMyBookmarks 호출됨, userId:", userId);

        if (!userId) {
            console.warn("⚠️ userId가 없습니다");
            alert("로그인이 필요합니다.");
            return;
        }

        const apiKey = localStorage.getItem("apiKey");
        const accessToken = localStorage.getItem("accessToken");

        if (!apiKey || !accessToken) {
            console.error("❌ 토큰이 없습니다. 로그인이 필요합니다.");
            alert("로그인이 필요합니다.");
            return;
        }

        setLoading(true);
        try {
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/bookmarks`, {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${apiKey} ${accessToken}`,
                },
                credentials: "include",
            });

            if (!res.ok) {
                throw new Error(`HTTP error! status: ${res.status}`);
            }

            const data = await res.json();
            console.log("📥 북마크 데이터:", data);

            const pinsOnly = extractArray(data.data).map((b: any) => b.pin ?? b);
            const normalized = normalizePins(pinsOnly);

            const pinsWithTags = await loadTagsForPins(normalized);
            setPins(pinsWithTags);
            setAllLoadedPins(pinsWithTags);
            setMode("bookmark");

            // ✅ 모드 변경 시 필터 초기화
            setSelectedTags([]);
            console.log("✅ 북마크 로드 완료:", pinsWithTags.length, "개");
        } catch (e) {
            console.error("❌ 북마크 핀 로드 실패:", e);
            setPins([]);
            setAllLoadedPins([]);
            alert("북마크를 불러오는데 실패했습니다.");
        } finally {
            setLoading(false);
        }
    };

    /* =========================================================
       ✅ 좋아요한 핀 로드
    ========================================================= */
    const loadLikedPins = async () => {
        console.log("❤️ loadLikedPins 호출됨, userId:", userId);

        if (!userId) {
            console.warn("⚠️ userId가 없습니다");
            alert("로그인이 필요합니다.");
            return;
        }

        const apiKey = localStorage.getItem("apiKey");
        const accessToken = localStorage.getItem("accessToken");

        if (!apiKey || !accessToken) {
            console.error("❌ 토큰이 없습니다. 로그인이 필요합니다.");
            alert("로그인이 필요합니다.");
            return;
        }

        setLoading(true);
        try {
            const res = await fetch(
                `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/user/${userId}/likespins`,
                {
                    method: "GET",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": `Bearer ${apiKey} ${accessToken}`,
                    },
                    credentials: "include",
                }
            );

            if (!res.ok) {
                throw new Error(`HTTP error! status: ${res.status}`);
            }

            const data = await res.json();
            console.log("📥 좋아요 데이터:", data);

            const likedArray = extractArray(data.data);
            const normalized = normalizePins(likedArray);

            const pinsWithTags = await loadTagsForPins(normalized);
            setPins(pinsWithTags);
            setAllLoadedPins(pinsWithTags);
            setMode("liked");

            // ✅ 모드 변경 시 필터 초기화
            setSelectedTags([]);
            console.log("✅ 좋아요 로드 완료:", pinsWithTags.length, "개");
        } catch (e) {
            console.error("❌ 좋아요 핀 로드 실패:", e);
            setPins([]);
            setAllLoadedPins([]);
            alert("좋아요한 핀을 불러오는데 실패했습니다.");
        } finally {
            setLoading(false);
        }
    };

    /* =========================================================
       ✅ 핀 클릭 시 태그 로드
    ========================================================= */
    const ensurePinTagsLoaded = async (pin: PinDto) => {
        try {
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/pins/${pin.id}/tags`);
            const data = await res.json();

            const tagsArray = Array.isArray(data.data?.tags) ? data.data.tags : [];
            const tagNames = tagsArray.map((t: any) => t.keyword);

            return {...pin, tags: tagNames};
        } catch (e) {
            console.error("핀 태그 로드 실패:", e);
            return {...pin, tags: [] as string[]};
        }
    };

    const loadTagsForPins = async (pinsToLoad: PinDto[]): Promise<PinDto[]> => {
        try {
            const pinsWithTags = await Promise.all(
                pinsToLoad.map(async (pin) => {
                    try {
                        const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/pins/${pin.id}/tags`);
                        const data = await res.json();
                        const tagsArray = Array.isArray(data.data?.tags) ? data.data.tags : [];
                        const tagNames = tagsArray.map((t: any) => t.keyword);
                        return {...pin, tags: tagNames};
                    } catch (e) {
                        console.error(`핀 ${pin.id} 태그 로드 실패:`, e);
                        return {...pin, tags: []};
                    }
                })
            );
            return pinsWithTags;
        } catch (e) {
            console.error("태그 일괄 로드 실패:", e);
            return pinsToLoad;
        }
    };

    /* =========================================================
       ✅ export
    ========================================================= */
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
        reloadTags,
        loadTagsForPins,
    };
}