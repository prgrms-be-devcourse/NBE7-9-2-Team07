"use client";

import Script from "next/script";
import { useEffect, useState } from "react";
import Sidebar from "../../components/Sidebar";
import PostModal from "../../components/PostModal";
import CreatePostModal from "../../components/CreatePostModal";
import { usePins } from "../../hooks/usePins";
import { useKakaoMap } from "../../hooks/useKakaoMap";
import { apiCreatePin } from "../../lib/pincoApi";
import { useAuth } from "@/context/AuthContext"; // ✅ 로그인 상태 관리
import { Heart, Star, LogOut } from "lucide-react"; // ✅ 아이콘 추가

export default function PinCoMainPage() {
    const { user, logout } = useAuth(); // ✅ 로그인 유저 정보
    const {
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
        loadLikedPins, // ✅ 좋아요한 핀 보기
        ensurePinTagsLoaded,
    } = usePins({ lat: 37.5665, lng: 126.978 }, user?.id ?? null); // ✅ userId 전달

    const [kakaoReady, setKakaoReady] = useState(false);
    useEffect(() => {
        const t = setInterval(() => {
            const w = window as any;
            if (w.kakao?.maps) {
                w.kakao.maps.load(() => setKakaoReady(true));
                clearInterval(t);
            }
        }, 80);
        return () => clearInterval(t);
    }, []);

    useKakaoMap({
        pins,
        center,
        onSelectPin: async (p) => {
            const withTags = await ensurePinTagsLoaded(p);
            setSelectedPin(withTags);
            setCenter({ lat: withTags.latitude, lng: withTags.longitude });
        },
        kakaoReady,
        onCenterChange: (lat, lng) => {
        setCenter({ lat, lng });
    }, // ✅ 추가
    });

    const [radius, setRadius] = useState(1000.0);
    // ✅ 화면 대각선 길이 기반으로 반지름 자동 계산
    const updateRadiusFromScreen = () => {
        const kakao = (window as any).kakao;
        const map = (window as any).mapRef;
        if (!kakao?.maps || !map) return;

        const bounds = map.getBounds();
        const sw = bounds.getSouthWest();
        const ne = bounds.getNorthEast();

        // 🔹 geometry 없이 거리 계산 (Haversine)
        const R = 6371000; // m
        const toRad = (deg: number) => (deg * Math.PI) / 180;
        const dLat = toRad(ne.getLat() - sw.getLat());
        const dLng = toRad(ne.getLng() - sw.getLng());
        const a =
            Math.sin(dLat / 2) ** 2 +
            Math.cos(toRad(sw.getLat())) *
            Math.cos(toRad(ne.getLat())) *
            Math.sin(dLng / 2) ** 2;
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        const diagonal = R * c;

        const newRadius = diagonal / 2;
        setRadius(newRadius);
        console.log("📏 화면 반지름:", newRadius.toFixed(2), "m");
    };


    // ✅ 지도 이동/확대/축소 시 반지름 갱신
    useEffect(() => {
        if (!kakaoReady) return;

        const kakao = (window as any).kakao;
        const map = (window as any).mapRef;
        if (!kakao?.maps || !map) return;

        kakao.maps.event.addListener(map, "idle", updateRadiusFromScreen);
        updateRadiusFromScreen(); // 초기 한 번 실행

        return () => {
            kakao.maps.event.removeListener(map, "idle", updateRadiusFromScreen);
        };
    }, [kakaoReady]);

    const [showCreate, setShowCreate] = useState(false);
    const handleCreate = async (content: string) => {
        try {
            await apiCreatePin(center.lat, center.lng, content);
            setShowCreate(false);
            if (mode === "nearby") await loadNearbyPins(center.lat, center.lng);
            else if (mode === "tag") await applyTagFilter(selectedTags);
            else if (mode === "bookmark") await loadMyBookmarks();
            else if (mode === "liked") await loadLikedPins();
            else if (mode === "screen") await loadAllPins(center.lat, center.lng, radius);
            else await loadAllPins();
            alert("등록 완료 🎉");
        } catch (e) {
            alert("등록 실패 ❌");
            console.error(e);
        }
    };

    return (
        <div className="flex flex-col h-[calc(100vh-64px)] overflow-hidden">
            {/* ✅ KakaoMap SDK 필요 라이브러리 추가*/}
            <Script
                src={`//dapi.kakao.com/v2/maps/sdk.js?autoload=false&appkey=${process.env.NEXT_PUBLIC_KAKAO_APP_KEY}&libraries=clusterer`}
                strategy="afterInteractive"
            />

            <main className="flex flex-1 overflow-hidden">
                {/* 📍 PinCoMainPage.tsx 안의 Sidebar 부분 */}
                <Sidebar
                    pins={pins}
                    loading={loading}
                    mode={mode}
                    allTags={allTags}
                    selectedTags={selectedTags}
                    onChangeTags={async (next) => {
                        // ✅ 실제 선택/해제 모두 훅 메서드로 처리
                        await applyTagFilter(next);     // 빈 배열이면 내부에서 clearTagFilter 호출됨
                    }}
                    onClickAll={() => loadAllPins(center.lat,center.lng,radius)}
                    onClickNearBy={async () => {
                        await clearTagFilter();         // 전체 보기 + 태그버튼 전부 해제 + 리스트 갱신
                    }}
                    onClickMyBookmarks={() => loadMyBookmarks()}
                    onClickLikedPins={() => loadLikedPins()}
                    onSelectPin={async (p) => {
                        const withTags = await ensurePinTagsLoaded(p);
                        setSelectedPin(withTags);
                        setCenter({ lat: withTags.latitude, lng: withTags.longitude });
                    }}
                />



                <div className="flex-1 relative">
                    <div id="map" className="w-full h-full" />

                    {/* ✅ 로그인 상태 표시 */}
                    <div className="absolute top-4 left-4 bg-white p-3 px-5 rounded-lg shadow-md z-50">
                        <p className="text-sm text-gray-600">
                            👋 {user ? `${user.name} (${user.email})` : "로그인 안됨"}
                        </p>
                        {user && (
                            <button
                                onClick={logout}
                                className="text-xs text-red-500 hover:underline mt-1 flex items-center gap-1"
                            >
                                <LogOut size={14} /> 로그아웃
                            </button>
                        )}
                    </div>

                    <div className="absolute top-20 left-4 bg-white p-2 px-3 rounded-lg shadow-md z-50 text-xs">
                        <p className="text-gray-500">📍 현재 위치</p>
                        <p className="text-gray-700 font-mono">
                            {/* center 상태는 useKakaoMap에서 실시간으로 갱신됨 */}
                            {center.lat.toFixed(6)}, {center.lng.toFixed(6)}
                        </p>
                    </div>

                    {selectedPin && (
                        <PostModal
                            pin={selectedPin}
                            onClose={() => setSelectedPin(null)}
                            userId={user?.id ?? null}
                            onChanged={async () => {
                                if (mode === "nearby") await loadNearbyPins(center.lat, center.lng);
                                else if (mode === "tag") await applyTagFilter(selectedTags);
                                else if (mode === "bookmark") await loadMyBookmarks();
                                else if (mode === "liked") await loadLikedPins();
                                else await loadAllPins();
                            }}
                        />
                    )}

                    {showCreate && (
                        <CreatePostModal
                            lat={center.lat}        // ✅ 중심 좌표 전달
                            lng={center.lng}
                            userId={user?.id ?? null}
                            onClose={() => setShowCreate(false)}
                            onCreated={async () => {
                                // 새로 등록한 핀 반영
                                if (mode === "nearby") await loadNearbyPins(center.lat, center.lng);
                                else if (mode === "tag") await applyTagFilter(selectedTags);
                                else if (mode === "bookmark") await loadMyBookmarks();
                                else if (mode === "liked") await loadLikedPins();
                                else await loadAllPins();
                            }}
                        />
                    )}

                    {/* ✅ 핀 추가 버튼 */}
                    <button
                        className="absolute bottom-6 left-1/2 -translate-x-1/2 bg-blue-600 text-white px-5 py-3 rounded-lg shadow-lg hover:bg-blue-700 z-50"
                        onClick={() => {
                            if (!user) {
                                alert("로그인 후 이용 가능합니다.");
                                return;
                            }
                            setShowCreate(true);
                        }
                    }
                    >
                        + 핀 추가
                    </button>

                    {/* ✅ 확대/축소 컨트롤 */}
                    <div className="absolute bottom-6 right-6 flex flex-col gap-3 z-50">
                        <button
                            className="bg-white border rounded-full shadow-md p-3 hover:bg-gray-100"
                            onClick={() => (window as any).mapRef?.setLevel((window as any).mapRef.getLevel() + 1)}
                        >
                            －
                        </button>
                        <button
                            className="bg-white border rounded-full shadow-md p-3 hover:bg-gray-100"
                            onClick={() => (window as any).mapRef?.setLevel((window as any).mapRef.getLevel() - 1)}
                        >
                            ＋
                        </button>
                    </div>
                </div>
            </main>

            <style jsx global>{`
        @keyframes fadeIn {
          from {
            opacity: 0;
            transform: translateY(10px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
        .animate-fadeIn {
          animation: fadeIn 0.2s ease-out;
        }
      `}</style>
        </div>
    );
}
function setRadius(newRadius: number) {
    throw new Error("Function not implemented.");
}

