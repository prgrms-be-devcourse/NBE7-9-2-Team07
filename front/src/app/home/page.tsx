"use client";

import { useEffect, useState } from "react";
import Script from "next/script";
import { Plus, Search, X, ZoomIn, ZoomOut } from "lucide-react";
import { fetchApi } from "@/lib/client";
import { useAuth } from "@/context/AuthContext";

// ✅ 쿠키 읽기 함수
const getCookie = (name: string) => {
    if (typeof document === "undefined") return null;
    const match = document.cookie.match(new RegExp("(^| )" + name + "=([^;]+)"));
    return match ? decodeURIComponent(match[2]) : null;
};

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
    const [postContent, setPostContent] = useState("");
    const [showPostForm, setShowPostForm] = useState(false);
    const [loading, setLoading] = useState(true);
    const { isLoggedIn } = useAuth();

    // ✅ 반경 1km 내 핀 조회
    const fetchNearbyPins = async (lat?: number, lng?: number) => {
        setLoading(true);
        try {
            const targetLat = lat ?? currentLocation?.lat;
            const targetLng = lng ?? currentLocation?.lng;
            if (!targetLat || !targetLng) return;

            const res = await fetchApi<any[]>(`/api/pins?latitude=${targetLat}&longitude=${targetLng}&radius=1`, {
                method: "GET",
            });

            const normalized = res.map((pin) => ({
                id: pin.id,
                latitude: pin.latitude,
                longitude: pin.longitude,
                createdAt: new Date().toISOString(),
                post: {
                    id: pin.id * 1000,
                    content: pin.title ?? "내용 없음",
                    createdAt: new Date().toISOString(),
                    modifiedAt: new Date().toISOString(),
                },
            }));

            // ✅ 기존 핀 유지 + 새로운 핀 추가 (중복 제거)
            setPins((prev) => {
                const existingMap = new Map(prev.map((p) => [p.id, p]));
                normalized.forEach((newPin) => {
                    existingMap.set(newPin.id, {
                        ...existingMap.get(newPin.id),
                        ...newPin,
                        post: existingMap.get(newPin.id)?.post || newPin.post, // ✅ 기존 post 보존
                    });
                });
                return Array.from(existingMap.values());
            });

            console.log("📍 반경 1km 핀 갱신 완료:", normalized);
        } catch (err) {
            console.error("주변 핀 조회 실패:", err);
        } finally {
            setLoading(false);
        }
    };

    // ✅ 모든 핀 조회 (/api/pins/all)
    const fetchAllPins = async () => {
        setLoading(true);

        if (!mapInstance) return;

        try {
            const pins = await fetchApi<Pin[]>("/api/pins/all", { method: "GET" }); // ✅ 수정
            console.log("🌍 모든 핀 불러오기 완료:", pins);

            setPins((prev) => {
                const existingIds = new Set(prev.map((p) => p.id));
                const merged = [...prev, ...pins.filter((p) => !existingIds.has(p.id))];
                return merged;
            });
        } catch (err) {
            console.error("🚨 모든 핀 불러오기 실패:", err);
        }
        setLoading(false);

    };

    // ✅ 게시글 전체 조회 (PostDto 기반)
    const fetchAllPosts = async () => {
        setLoading(true);
        try {
            const posts = await fetchApi<any[]>("/api/posts", { method: "GET" }); // 바로 배열 받음

            if (!Array.isArray(posts)) {
                console.error("🚨 posts 데이터가 배열이 아닙니다:", posts);
                return;
            }

            const convertedPins = posts.map((p) => ({
                id: p.pin.id,
                latitude: p.pin.latitude,
                longitude: p.pin.longitude,
                createdAt: p.pin.createAt ?? new Date().toISOString(),
                post: {
                    id: p.id,
                    content: p.content,
                    createdAt: p.createAt,
                    modifiedAt: p.modifiedAt,
                },
            }));

            setPins(convertedPins);
            console.log("🗺️ 게시글 기반 핀 목록:", convertedPins);
        } catch (err) {
            console.error("🚨 게시글 조회 실패:", err);
        }
        setLoading(false);
    };

    // 🔹 게시글 생성 로직
    const handleCreatePost = async () => {
        if (!isLoggedIn) {
            alert("로그인이 필요합니다 🔒");
            window.location.href = "/user/login";
            return;
        }

        if (!currentLocation) return;
        if (!postContent.trim()) return alert("내용을 입력해주세요!");

        const req: CreatePostRequest = {
            latitude: currentLocation.lat,
            longitude: currentLocation.lng,
            content: postContent,
        };

        try {
            // ✅ 백엔드에 게시글 등록 요청
            const res = await fetchApi("/api/posts", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(req),
            });

            const postData = res // RsData 구조 기준: { errorCode, msg, data }
            console.log("📦 서버 응답:", res);

            if (!postData) {
                alert("게시글 생성 실패 ❌");
                return;
            }

            // ✅ 백엔드에서 반환된 PostDto → Pin 형태로 변환
            const newPin: Pin = {
                id: postData.pin?.id ?? Date.now(), // pinId
                latitude: postData.pin?.latitude ?? req.latitude,
                longitude: postData.pin?.longitude ?? req.longitude,
                createdAt: postData.pin?.createAt ?? new Date().toISOString(),
                post: {
                    id: postData.id,
                    content: postData.content,
                    createdAt: postData.createAt,
                    modifiedAt: postData.modifiedAt,
                },
            };

            // ✅ 지도 핀 목록에 추가
            setPins((prev) => [...prev, newPin]);
            alert("게시글과 핀이 성공적으로 등록되었습니다 🎉");
        } catch (err) {
            console.error("🚨 게시글 생성 실패:", err);
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
            level: 4,
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
        fetchAllPosts();
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

        // ✅ 기존 마커 제거
        mapInstance.markers?.forEach((m: any) => m.setMap(null));
        mapInstance.markers = [];

        // ✅ 클러스터러 생성 (도심용 세밀한 설정)
        const clusterer = new kakao.maps.MarkerClusterer({
            map: mapInstance,
            averageCenter: true,
            minLevel: 3, // 👈 줌을 아주 약간만 축소해도 묶이게
            disableClickZoom: false,
            gridSize: 60, // 👈 클러스터링 기준 거리(px). 작을수록 더 세밀하게 나뉨
            styles: [
                {
                    width: "32px",
                    height: "32px",
                    background: "rgba(59, 130, 246, 0.95)", // Tailwind 'blue-500'
                    color: "#fff",
                    borderRadius: "50%",
                    textAlign: "center",
                    lineHeight: "32px",
                    fontWeight: "600",
                    fontSize: "12px",
                    boxShadow: "0 0 6px rgba(0,0,0,0.2)",
                },
            ],
        });

        // ✅ 개별 마커 생성
        const markers = pins.map((pin) => {
            const marker = new kakao.maps.Marker({
                position: new kakao.maps.LatLng(pin.latitude, pin.longitude),
            });

            const info = new kakao.maps.InfoWindow({
                content: `<div style="padding:6px; font-size:12px;">📍 ${pin.post?.content ?? "내용 없음"}</div>`,
            });

            kakao.maps.event.addListener(marker, "mouseover", () => info.open(mapInstance, marker));
            kakao.maps.event.addListener(marker, "mouseout", () => info.close());

            kakao.maps.event.addListener(marker, "click", async () => {
                if (!isLoggedIn) {
                    alert("로그인이 필요합니다 🔒");
                    window.location.href = "/user/login";
                    return;
                }

                try {
                    const post = await fetchApi(`/api/posts/${pin.id}`, { method: "GET" });
                    if (!post) {
                        alert("이 핀에는 게시글이 없습니다 ❌");
                        return;
                    }

                    setSelectedPin({
                        ...pin,
                        post: {
                            id: post.id,
                            content: post.content,
                            createdAt: post.createAt,
                            modifiedAt: post.modifiedAt,
                        },
                    });
                } catch (err) {
                    console.error("🚨 게시글 불러오기 실패:", err);
                }
            });

            return marker;
        });

        // ✅ 클러스터러에 마커 등록
        clusterer.addMarkers(markers);
        mapInstance.markers = markers;
    }, [pins, mapInstance, isLoggedIn]);

    // ✅ 거리 계산 함수 (하버사인 공식)
    const getDistance = (lat1: number, lon1: number, lat2: number, lon2: number) => {
        const R = 6371; // 지구 반지름 (km)
        const dLat = (lat2 - lat1) * (Math.PI / 180);
        const dLon = (lon2 - lon1) * (Math.PI / 180);
        const a =
            Math.sin(dLat / 2) ** 2 +
            Math.cos(lat1 * (Math.PI / 180)) *
            Math.cos(lat2 * (Math.PI / 180)) *
            Math.sin(dLon / 2) ** 2;
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    };

    // ✅ 주변 보기 모드일 때만 반경 1km 이내 표시
    const filteredPins = pins.filter((p) => {
        if (!currentLocation) return false;

        // 거리 계산
        const distance = getDistance(
            currentLocation.lat,
            currentLocation.lng,
            p.latitude,
            p.longitude
        );

        // 주변 보기 모드일 때만 거리 제한 적용
        const inRange = viewMode === "nearby" ? distance <= 1 : true;

        // 검색어 필터
        const matchQuery = p.post?.content
            .toLowerCase()
            .includes(searchQuery.toLowerCase());

        return inRange && matchQuery;
    });

    return (
        <div className="flex flex-col h-[calc(100vh-64px)] overflow-hidden">
            <Script
                src={`//dapi.kakao.com/v2/maps/sdk.js?appkey=${process.env.NEXT_PUBLIC_KAKAO_APP_KEY}&autoload=false&libraries=clusterer`}
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
                                    fetchAllPosts(); // ✅ 새 함수 호출
                                }}
                                className={`px-2 py-1 text-xs rounded-md ${viewMode === "nearby" ? "bg-blue-600 text-white" : "bg-gray-100 text-gray-700"
                                    }`}
                            >
                                주변 보기
                            </button>
                            {/* 모두 보기 버튼 */}
                            <button
                                onClick={() => {
                                    setViewMode("all");
                                    fetchAllPins(); // ✅ 서버에서 전체 핀 불러오기
                                    fetchAllPosts(); // ✅ 새 함수 호출
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
                        {loading ? (
                            <p className="text-gray-400 text-sm text-center py-6">불러오는 중입니다... ⏳</p>
                        ) : filteredPins.length > 0 ? (
                            filteredPins.map((pin) => (
                                <div
                                    key={pin.id}
                                    onClick={async () => {
                                        if (!isLoggedIn) {
                                            alert("로그인이 필요합니다 🔒");
                                            window.location.href = "/user/login";
                                            return;
                                        }
                                        if (mapInstance) {
                                            const kakao = window.kakao;
                                            const moveLatLon = new kakao.maps.LatLng(pin.latitude, pin.longitude);
                                            mapInstance.panTo(moveLatLon);
                                        }

                                        try {
                                            const post = await fetchApi(`/api/posts/${pin.id}`, { method: "GET" });
                                            if (!post) {
                                                alert("해당 핀에 게시글이 없습니다 ❌");
                                                return;
                                            }

                                            setSelectedPin({
                                                ...pin,
                                                post: {
                                                    id: post.id,
                                                    content: post.content,
                                                    createdAt: post.createAt,
                                                    modifiedAt: post.modifiedAt,
                                                },
                                            });
                                        } catch (err) {
                                            console.error("🚨 게시글 불러오기 실패:", err);
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

                    {/* 🔹 핀 추가 버튼 */}
                    <button
                        className="absolute bottom-6 left-1/2 transform -translate-x-1/2 bg-blue-600 text-white px-5 py-3 rounded-lg shadow-lg hover:bg-blue-700 z-50 flex items-center gap-2"
                        onClick={() => setShowPostForm(true)} // ✅ 폼 열기
                    >
                        <Plus className="w-5 h-5" /> 핀 추가
                    </button>

                    {/* 🔹 게시글 입력 폼 (모달) */}
                    {showPostForm && (
                        <div className="absolute inset-0 bg-black/50 flex items-center justify-center z-50">
                            <div className="bg-white rounded-xl shadow-xl w-96 max-w-[90%] relative animate-fadeIn p-6">
                                <button
                                    className="absolute top-3 right-3 text-gray-500 hover:text-black"
                                    onClick={() => setShowPostForm(false)}
                                >
                                    <X className="w-5 h-5" />
                                </button>

                                <h2 className="text-lg font-semibold mb-3">📝 새 게시글 작성</h2>
                                <textarea
                                    value={postContent}
                                    onChange={(e) => setPostContent(e.target.value)}
                                    placeholder="게시글 내용을 입력하세요..."
                                    className="w-full border rounded-md p-2 h-32 text-sm resize-none mb-4"
                                />

                                <button
                                    onClick={handleCreatePost}
                                    className="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700"
                                >
                                    등록하기
                                </button>
                            </div>
                        </div>
                    )}

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
