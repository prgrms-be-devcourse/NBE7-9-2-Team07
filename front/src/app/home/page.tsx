"use client";

import { useEffect, useState } from "react";
import Script from "next/script";
import { Plus, Search, X, ZoomIn, ZoomOut } from "lucide-react";
import { fetchApi } from "@/lib/client";

// ✅ 타입 정의
type Post = {
    id: number;
    content: string;
    createdAt: string;
    modifiedAt: string;
};

type Pin = {
    id: number;
    latitude: number;
    longitude: number;
    createdAt: string;
    post: Post | null;
};

type CreatePostRequest = {
    latitude: number;
    longitude: number;
    content: string;
};

const initialPins: Pin[] = [
    {
        id: 1,
        latitude: 37.5655,
        longitude: 126.978,
        createdAt: "2025-10-14T09:00:00",
        post: {
            id: 101,
            content: "오늘은 서울 시청 근처 카페에서 작업했어요 ☕",
            createdAt: "2025-10-14T10:12:33",
            modifiedAt: "2025-10-14T12:45:10",
        },
    },
    {
        id: 2,
        latitude: 37.57,
        longitude: 126.982,
        createdAt: "2025-10-14T08:30:00",
        post: {
            id: 102,
            content: "종로 거리에 새로 생긴 카페 ☕ 분위기 굿!",
            createdAt: "2025-10-14T09:47:02",
            modifiedAt: "2025-10-14T10:02:55",
        },
    },
    {
        id: 3,
        latitude: 37.5555,
        longitude: 126.978,
        createdAt: "2025-10-14T09:00:00",
        post: {
            id: 103,
            content: "오오오 ☕",
            createdAt: "2025-10-14T10:12:33",
            modifiedAt: "2025-10-14T12:45:10",
        },
    },
    {
        id: 4,
        latitude: 37.6,
        longitude: 126.982,
        createdAt: "2025-10-14T08:30:00",
        post: {
            id: 104,
            content: "ㅇㅇㅇ!",
            createdAt: "2025-10-14T09:47:02",
            modifiedAt: "2025-10-14T10:02:55",
        },
    },
];

export default function PinCoMainPage() {
    const [pins, setPins] = useState<Pin[]>(initialPins);
    const [searchQuery, setSearchQuery] = useState("");
    const [isMapLoaded, setIsMapLoaded] = useState(false);
    const [mapInstance, setMapInstance] = useState<any>(null);
    const [currentLocation, setCurrentLocation] = useState<{ lat: number; lng: number } | null>(null);
    const [selectedPin, setSelectedPin] = useState<Pin | null>(null);
    const [viewMode, setViewMode] = useState<"nearby" | "all">("nearby");

    // ✅ 반경 1km 내 핀 조회
    const fetchNearbyPins = async (lat?: number, lng?: number) => {
        const targetLat = lat ?? currentLocation?.lat;
        const targetLng = lng ?? currentLocation?.lng;
        if (!targetLat || !targetLng) return;

        try {
            // ✅ [1단계] 실제 API 연결 시 이 부분만 활성화
            // const res = await fetchApi<Pin[]>(`/api/pins?latitude=${targetLat}&longitude=${targetLng}&radius=1`, {
            //   method: "GET",
            // });
            // setPins(res);
            // console.log("📍 반경 1km 핀 조회 완료:", res);

            // ✅ [2단계] 현재는 임시로 로컬 데이터 필터링
            const R = 6371;
            const within1Km = initialPins.filter((pin) => {
                const dLat = ((pin.latitude - targetLat) * Math.PI) / 180;
                const dLng = ((pin.longitude - targetLng) * Math.PI) / 180;
                const a =
                    Math.sin(dLat / 2) ** 2 +
                    Math.cos((targetLat * Math.PI) / 180) *
                    Math.cos((pin.latitude * Math.PI) / 180) *
                    Math.sin(dLng / 2) ** 2;
                const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
                return R * c <= 1;
            });

            setPins(within1Km);
            console.log("📍 로컬 반경 1km 필터 적용:", within1Km);
        } catch (err) {
            console.error("주변 핀 조회 실패:", err);
        }
    };

    // ✅ 모든 핀 조회 (/api/pins/all)
    const fetchAllPins = async () => {
        if (!mapInstance) return;

        const bounds = mapInstance.getBounds();
        const sw = bounds.getSouthWest();
        const ne = bounds.getNorthEast();

        const req = {
            radius: 10,
            minLatitude: sw.getLat(),
            maxLatitude: ne.getLat(),
            minLongitude: sw.getLng(),
            maxLongitude: ne.getLng(),
        };

        try {
            // ✅ 실제 API 연결 시 이 부분만 활성화
            // const res = await fetchApi<Pin[]>("/api/pins/all", {
            //     method: "POST",
            //     headers: { "Content-Type": "application/json" },
            //     body: JSON.stringify(req),
            // });
            setPins(res);
            console.log("🌍 모든 핀 불러오기 완료:", res);
        } catch (err) {
            console.error("모든 핀 불러오기 실패:", err);
        }
    };

    // ✅ 게시글 작성 완료 → 서버로 전송
    const handleCreatePost = async () => {
        if (!currentLocation) return;
        if (!postContent.trim()) return alert("내용을 입력해주세요!");

        const req: CreatePostRequest = {
            latitude: currentLocation.lat,
            longitude: currentLocation.lng,
            content: postContent,
        };

        try {
            // ✅ 실제 API 연결 시 이 부분만 활성화
            // const res = await fetchApi<Pin>("/api/posts", {
            //     method: "POST",
            //     headers: { "Content-Type": "application/json" },
            //     body: JSON.stringify(req),
            // });

            setPins((prev) => [...prev, res]);
            alert("게시글과 핀이 성공적으로 등록되었습니다 🎉");
        } catch (err) {
            console.error("게시글 생성 실패:", err);
            alert("서버 통신 중 오류가 발생했습니다 ❌");
        } finally {
            setShowPostForm(false);
            setPostContent("");
        }
    };

    // ✅ Kakao SDK 로드
    useEffect(() => {
        const checkKakao = () => {
            if (window.kakao && window.kakao.maps) {
                window.kakao.maps.load(() => setIsMapLoaded(true));
            } else setTimeout(checkKakao, 100);
        };
        checkKakao();
    }, []);

    // ✅ 기본 위치
    useEffect(() => {
        setCurrentLocation({ lat: 37.5665, lng: 126.978 });
    }, []);

    // ✅ 지도 초기화
    useEffect(() => {
        if (!isMapLoaded || !currentLocation) return;
        const kakao = window.kakao;
        const container = document.getElementById("map");
        if (!container) return;

        const map = new kakao.maps.Map(container, {
            center: new kakao.maps.LatLng(currentLocation.lat, currentLocation.lng),
            level: 5,
        });
        setMapInstance(map);

        new kakao.maps.Marker({
            map,
            position: new kakao.maps.LatLng(currentLocation.lat, currentLocation.lng),
            image: new kakao.maps.MarkerImage(
                "https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png",
                new kakao.maps.Size(34, 45)
            ),
        });

        new kakao.maps.Circle({
            map,
            center: new kakao.maps.LatLng(currentLocation.lat, currentLocation.lng),
            radius: 1000,
            strokeWeight: 2,
            strokeColor: "#4F46E5",
            strokeOpacity: 0.6,
            fillColor: "#6366F140",
            fillOpacity: 0.4,
        });

        fetchNearbyPins();
    }, [isMapLoaded, currentLocation]);

    // ✅ 지도 드래그 이벤트 (모드별로 동작)
    useEffect(() => {
        if (!mapInstance) return;
        const kakao = window.kakao;

        // ✅ 이미 등록된 dragend 리스너 제거를 위해 플래그 저장
        if (mapInstance._dragendHandler) {
            kakao.maps.event.removeListener(mapInstance, "dragend", mapInstance._dragendHandler);
        }

        // ✅ 새로운 리스너 등록
        const handler = () => {
            const center = mapInstance.getCenter();
            if (viewMode === "nearby") fetchNearbyPins(center.getLat(), center.getLng());
        };

        kakao.maps.event.addListener(mapInstance, "dragend", handler);
        mapInstance._dragendHandler = handler; // ✅ 플래그로 보관 (remove 시 필요)
    }, [mapInstance, viewMode]);


    // ✅ 마커 렌더링
    useEffect(() => {
        if (!mapInstance) return;
        const kakao = window.kakao;

        mapInstance.markers?.forEach((m: any) => m.setMap(null));
        mapInstance.markers = [];

        pins.forEach((pin) => {
            const marker = new kakao.maps.Marker({
                map: mapInstance,
                position: new kakao.maps.LatLng(pin.latitude, pin.longitude),
            });

            const info = new kakao.maps.InfoWindow({
                content: `<div style="padding:6px; font-size:12px;">📍 ${pin.post?.content ?? "내용 없음"}</div>`,
            });

            kakao.maps.event.addListener(marker, "mouseover", () => info.open(mapInstance, marker));
            kakao.maps.event.addListener(marker, "mouseout", () => info.close());
            kakao.maps.event.addListener(marker, "click", () => setSelectedPin(pin));

            mapInstance.markers.push(marker);
        });
    }, [pins, mapInstance]);

    const filteredPins = pins.filter((p) =>
        p.post?.content.toLowerCase().includes(searchQuery.toLowerCase())
    );

    return (
        <div className="flex flex-col h-[calc(100vh-64px)] overflow-hidden">
            <Script
                src={`//dapi.kakao.com/v2/maps/sdk.js?appkey=${process.env.NEXT_PUBLIC_KAKAO_APP_KEY}&autoload=false`}
                strategy="afterInteractive"
            />

            <main className="flex flex-1 overflow-hidden">
                {/* 왼쪽 리스트 */}
                <div className="bg-white border-r w-80 p-4 flex flex-col gap-4 overflow-y-auto">
                    <div className="flex justify-between items-center mb-2">
                        <h3 className="text-gray-700 font-semibold">📍 핀 목록</h3>
                        <div className="flex gap-2">
                            <button
                                onClick={() => {
                                    setViewMode("nearby");
                                    fetchNearbyPins();
                                }}
                                className={`px-2 py-1 text-xs rounded-md ${viewMode === "nearby" ? "bg-blue-600 text-white" : "bg-gray-100 text-gray-700"
                                    }`}
                            >
                                주변 보기
                            </button>
                            <button
                                onClick={() => {
                                    setViewMode("all");
                                    setPins(initialPins);
                                    fetchAllPins(); // ✅ 서버에서 전체 핀 불러오기
                                }}
                                className={`px-2 py-1 text-xs rounded-md ${viewMode === "all" ? "bg-blue-600 text-white" : "bg-gray-100 text-gray-700"
                                    }`}
                            >
                                모두 보기
                            </button>
                        </div>
                    </div>

                    <div className="relative">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
                        <input
                            type="text"
                            placeholder="게시글 검색..."
                            value={searchQuery}
                            onChange={(e) => setSearchQuery(e.target.value)}
                            className="pl-10 w-full border rounded-md p-2 text-sm"
                        />
                    </div>

                    <div className="space-y-2">
                        {filteredPins.length > 0 ? (
                            filteredPins.map((pin) => (
                                <div
                                    key={pin.id}
                                    onClick={() => {
                                        setSelectedPin(pin);
                                        if (mapInstance) {
                                            const kakao = window.kakao;
                                            const moveLatLon = new kakao.maps.LatLng(pin.latitude, pin.longitude);
                                            mapInstance.panTo(moveLatLon); // ✅ 해당 핀 위치로 부드럽게 이동
                                        }
                                    }}
                                    className="border rounded-md p-3 cursor-pointer hover:bg-blue-50 transition"
                                >
                                    <p className="text-sm text-gray-800 line-clamp-2">{pin.post?.content}</p>
                                    <div className="flex justify-between text-xs text-gray-500 mt-1">
                                        <span>{pin.post?.createdAt.slice(0, 10)}</span>
                                        <span>{pin.post?.modifiedAt.slice(0, 10)}</span>
                                    </div>
                                </div>
                            ))
                        ) : (
                            <p className="text-gray-400 text-sm text-center py-6">게시글이 없습니다 😢</p>
                        )}
                    </div>
                </div>

                {/* 지도 */}
                <div className="flex-1 relative">
                    <div id="map" className="w-full h-full" />

                    {/* ✅ 게시물 모달 (중앙에 뜸) */}
                    {selectedPin && (
                        <div className="absolute inset-0 bg-black/50 flex items-center justify-center z-50">
                            <div className="bg-white rounded-xl shadow-xl w-96 max-w-[90%] relative animate-fadeIn">
                                <button
                                    className="absolute top-3 right-3 text-gray-500 hover:text-black"
                                    onClick={() => setSelectedPin(null)}
                                >
                                    <X className="w-5 h-5" />
                                </button>
                                <div className="p-6">
                                    <h2 className="text-lg font-semibold mb-3">📝 게시글 내용</h2>
                                    {selectedPin.post ? (
                                        <>
                                            <p className="text-gray-800 mb-6 leading-relaxed">
                                                {selectedPin.post.content}
                                            </p>
                                            <div className="flex justify-between text-sm text-gray-500 border-t pt-3">
                                                <span>작성일: {selectedPin.post.createdAt.slice(0, 10)}</span>
                                                <span>수정일: {selectedPin.post.modifiedAt.slice(0, 10)}</span>
                                            </div>
                                        </>
                                    ) : (
                                        <p className="text-gray-500 text-center py-6">
                                            등록된 내용이 없습니다 🕓
                                        </p>
                                    )}
                                </div>
                            </div>
                        </div>
                    )}

                    {/* 핀 추가 버튼 */}
                    <button
                        className="absolute bottom-6 left-1/2 transform -translate-x-1/2 bg-blue-600 text-white px-5 py-3 rounded-lg shadow-lg hover:bg-blue-700 z-50 flex items-center gap-2"
                        onClick={handleCreatePost}
                    >
                        <Plus className="w-5 h-5" /> 핀 추가
                    </button>

                    {/* 확대/축소 버튼 */}
                    <div className="absolute bottom-6 right-6 flex flex-col gap-3 z-50">
                        <button
                            className="bg-white border rounded-full shadow-md p-3 hover:bg-gray-100"
                            onClick={() => mapInstance && mapInstance.setLevel(mapInstance.getLevel() + 1)}
                        >
                            <ZoomOut className="w-5 h-5 text-gray-700" />
                        </button>
                        <button
                            className="bg-white border rounded-full shadow-md p-3 hover:bg-gray-100"
                            onClick={() => mapInstance && mapInstance.setLevel(mapInstance.getLevel() - 1)}
                        >
                            <ZoomIn className="w-5 h-5 text-gray-700" />
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
          animation: fadeIn 0.3s ease-out;
        }
      `}</style>
        </div>
    );
}
