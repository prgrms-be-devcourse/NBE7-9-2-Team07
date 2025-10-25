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
    tags?: string[]; // ✅ 태그 목록 추가
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
    const [loading, setLoading] = useState(false);
    const [mode, setMode] = useState<Mode>("nearby");
    const [center, setCenter] = useState(initialCenter);
    const [selectedPin, setSelectedPin] = useState<PinDto | null>(null);
    const [selectedTags, setSelectedTags] = useState<string[]>([]);
    const [allTags, setAllTags] = useState<TagDto[]>([]);

    /* =========================================================
       ✅ 공통 유틸 함수
    ========================================================= */

    /** 배열 또는 객체 응답을 안전하게 변환 */
    const extractArray = (data: any): any[] => {
        if (!data) return [];
        if (Array.isArray(data)) return data;
        if (Array.isArray(data?.pins)) return data.pins;
        if (Array.isArray(data?.tags)) return data.tags;
        return [];
    };

    /** ✅ 공통 정규화 함수 */
    const normalizePins = (arr: any[] | null | undefined): PinDto[] => {
        if (!Array.isArray(arr)) return [];
        return arr.map((p, idx) => {
            const pin = p.pin ?? p; // 중첩 구조 대응
            return {
                id: pin.id ?? idx + Math.random(), // id 없을 때 fallback
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
                tags: pin.tags ?? [], // ✅ 기본 태그 배열
            };
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

    /* =========================================================
       ✅ 태그 새로고침 함수 (외부에서 호출 가능)
    ========================================================= */
    const reloadTags = async () => {
        await fetchTags();
    };

    /* =========================================================
       ✅ 화면상 모든 핀 조회
    ========================================================= */
    const loadAllPins = async (lat?: number, lng?: number, radius?:number) => {

        setLoading(true);
        try {
            const apiKey = localStorage.getItem("apiKey");
            const accessToken = localStorage.getItem("accessToken");

            // 1. 기본 헤더 설정
            const headers: HeadersInit = {
                "Content-Type": "application/json",
            };

            // 2. ✅ 인증 정보가 모두 있을 때만 Authorization 헤더 추가
            if (apiKey && accessToken) {
                headers["Authorization"] = `Bearer ${apiKey} ${accessToken}`;
            }

            //url 설정
            const url = `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/pins?latitude=${lat ?? center.lat}&longitude=${lng ?? center.lng}&radius=${radius}`;

            const res = await fetch(
                url,
                {
                    method: "GET",
                    headers: headers, // 수정된 headers 객체를 사용
                    credentials: "include", // ✅ 쿠키 포함
                }
            );
            const data = await res.json();

            const pinArray = extractArray(data.data);
            setPins(normalizePins(pinArray));
            setMode("screen");
        } catch (e) {
            console.error("화면 전체 핀 로드 실패:", e);
            setPins([]);
        } finally {
            setLoading(false);
        }
    };

    // ✅ 첫 렌더링 시 자동 전체 핀 로드
    useEffect(() => {
        loadNearbyPins();
        // loadAllPins();
    }, []);

    /* =========================================================
       ✅ 주변 핀 조회
    ========================================================= */
    const loadNearbyPins = async (lat?: number, lng?: number) => {
        setLoading(true);
        try {
            const apiKey = localStorage.getItem("apiKey");
            const accessToken = localStorage.getItem("accessToken");

            // 1. 기본 헤더 설정
            const headers: HeadersInit = {
                "Content-Type": "application/json",
            };

            // 2. ✅ 인증 정보가 모두 있을 때만 Authorization 헤더 추가
            if (apiKey && accessToken) {
                headers["Authorization"] = `Bearer ${apiKey} ${accessToken}`;
            }

            //url 설정
            const url = `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/pins?latitude=${lat ?? center.lat}&longitude=${lng ?? center.lng}`;

            const res = await fetch(
                url,
                {
                    method: "GET",
                    headers: headers, // 수정된 headers 객체를 사용
                    credentials: "include", // ✅ 쿠키 포함
                }
            );
            const data = await res.json();

            const pinArray = extractArray(data.data);
            setPins(normalizePins(pinArray));
            setMode("nearby");
        } catch (e) {
            console.error("주변 핀 로드 실패:", e);
            setPins([]);
        } finally {
            setLoading(false);
        }
    };

    /* =========================================================
       ✅ 태그 기반 필터링
    ========================================================= */
    const applyTagFilter = async (tags: string[]) => {
        setSelectedTags(tags);
        if (tags.length === 0) return clearTagFilter();

        setLoading(true);
        try {
            const query = tags.map((t) => `keywords=${encodeURIComponent(t)}`).join("&");
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/tags/filter?${query}`);
            const data = await res.json();

            const filteredPins = extractArray(data.data);
            setPins(normalizePins(filteredPins));
            setMode("tag");
        } catch (e) {
            console.error("태그 필터 실패:", e);
            setPins([]);
        } finally {
            setLoading(false);
        }
    };

    /* =========================================================
       ✅ 태그 전체 해제
    ========================================================= */
    const clearTagFilter = async () => {
        setSelectedTags([]);
        await loadNearbyPins()
    };

    /* =========================================================
       ✅ 북마크 핀 로드
    ========================================================= */
    const loadMyBookmarks = async () => {
        if (!userId) {
            alert("로그인이 필요합니다.");
            return;
        }
        setLoading(true);
        try {
            const apiKey = localStorage.getItem("apiKey");
            const accessToken = localStorage.getItem("accessToken");

            if (!apiKey || !accessToken) {
                console.error("❌ 토큰이 없습니다. 로그인이 필요합니다.");
                alert("로그인이 필요합니다.");
                setLoading(false);
                return;
            }

            const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/bookmarks`,
                {
                    method: "GET",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": `Bearer ${apiKey} ${accessToken}`, // ✅ 인증 헤더 추가
                    },
                    credentials: "include", // ✅ 쿠키 포함
                }
            );

            const data = await res.json();

            const pinsOnly = extractArray(data.data).map((b: any) => b.pin ?? b);
            setPins(normalizePins(pinsOnly));
            setMode("bookmark");
        } catch (e) {
            console.error("북마크 핀 로드 실패:", e);
            setPins([]);
        } finally {
            setLoading(false);
        }
    };

    /* =========================================================
       ✅ 좋아요한 핀 로드
    ========================================================= */
    const loadLikedPins = async () => {
        if (!userId) {
            alert("로그인이 필요합니다.");
            return;
        }
        setLoading(true);
        try {
            const apiKey = localStorage.getItem("apiKey");
            const accessToken = localStorage.getItem("accessToken");

            if (!apiKey || !accessToken) {
                console.error("❌ 토큰이 없습니다. 로그인이 필요합니다.");
                alert("로그인이 필요합니다.");
                setLoading(false);
                return;
            }

            const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/user/${userId}/likespins`,
                {
                    method: "GET",
                    headers: {
                        "Content-Type": "application/json",
                        "Authorization": `Bearer ${apiKey} ${accessToken}`, // ✅ 인증 헤더 추가
                    },
                    credentials: "include", // ✅ 쿠키 포함
                }
            );

            const data = await res.json();

            const likedArray = extractArray(data.data);
            setPins(normalizePins(likedArray));
            setMode("liked");
        } catch (e) {
            console.error("좋아요 핀 로드 실패:", e);
            setPins([]);
        } finally {
            setLoading(false);
        }
    };

    /* =========================================================
       ✅ 핀 클릭 시 태그 로드 (수정된 핵심 부분)
    ========================================================= */
    const ensurePinTagsLoaded = async (pin: PinDto) => {
        try {
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/pins/${pin.id}/tags`);
            const data = await res.json();

            // ✅ 정확한 구조: { data: { pinId, tags: [...] } }
            const tagsArray = Array.isArray(data.data?.tags) ? data.data.tags : [];

            const tagNames = tagsArray.map((t: any) => t.keyword);

            // ✅ pin 객체에 tags 필드 추가
            return {...pin, tags: tagNames};
        } catch (e) {
            console.error("핀 태그 로드 실패:", e);
            return {...pin, tags: [] as string[]};
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
        reloadTags, // ✅ 태그 새로고침 함수 export
    };
}
