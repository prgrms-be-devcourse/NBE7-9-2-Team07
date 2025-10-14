"use client";

import { useEffect, useState } from "react";
import Script from "next/script";
import { MapPin, Plus, Search, X, ZoomIn, ZoomOut } from "lucide-react";

// ✅ Mock 데이터 (Pin + Optional Post)
const mockPins = [
  {
    id: 1,
    latitude: 37.5655,
    longitude: 126.9780,
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
    latitude: 37.5700,
    longitude: 126.9820,
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
    latitude: 37.5600,
    longitude: 126.9750,
    createdAt: "2025-10-13T21:30:00",
    post: null, // 게시글이 없는 핀
  },
  {
    id: 4,
    latitude: 37.5635,
    longitude: 126.9875,
    createdAt: "2025-10-13T19:00:00",
    post: {
      id: 104,
      content: "을지로 골목 산책 중 🌿 레트로 감성 최고!",
      createdAt: "2025-10-13T19:40:12",
      modifiedAt: "2025-10-13T20:11:30",
    },
  },
  {
    id: 5,
    latitude: 37.5685,
    longitude: 126.9700,
    createdAt: "2025-10-13T17:00:00",
    post: {
      id: 105,
      content: "덕수궁 돌담길 산책 중 🍂",
      createdAt: "2025-10-13T17:20:12",
      modifiedAt: "2025-10-13T17:58:05",
    },
  },
  {
    id: 6,
    latitude: 37.5750,
    longitude: 126.9800,
    createdAt: "2025-10-13T15:33:00",
    post: {
      id: 106,
      content: "광화문 앞 분수대에서 사진 한 컷 📸",
      createdAt: "2025-10-13T15:33:12",
      modifiedAt: "2025-10-13T15:50:40",
    },
  },
];

// 거리 계산 (하버사인 공식)
function getDistance(lat1: number, lng1: number, lat2: number, lng2: number) {
  const R = 6371;
  const dLat = ((lat2 - lat1) * Math.PI) / 180;
  const dLon = ((lng2 - lng1) * Math.PI) / 180;
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos((lat1 * Math.PI) / 180) *
      Math.cos((lat2 * Math.PI) / 180) *
      Math.sin(dLon / 2) ** 2;
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}

export default function PinCoMainPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [isMapLoaded, setIsMapLoaded] = useState(false);
  const [currentLocation, setCurrentLocation] = useState<{ lat: number; lng: number } | null>(null);
  const [selectedPin, setSelectedPin] = useState<typeof mockPins[0] | null>(null);
  const [mapInstance, setMapInstance] = useState<any>(null);
  const [showAddModal, setShowAddModal] = useState(false);
  const [showPostForm, setShowPostForm] = useState(false);

  // Kakao SDK 로드 감시
  useEffect(() => {
    const checkKakao = () => {
      if (window.kakao && window.kakao.maps) {
        window.kakao.maps.load(() => setIsMapLoaded(true));
      } else {
        setTimeout(checkKakao, 100);
      }
    };
    checkKakao();
  }, []);

  // 기본 위치 (서울 시청)
  useEffect(() => {
    setCurrentLocation({ lat: 37.5665, lng: 126.9780 });
  }, []);

  // 지도 초기화
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

    // 현재 위치 마커
    new kakao.maps.Marker({
      map,
      position: new kakao.maps.LatLng(currentLocation.lat, currentLocation.lng),
      image: new kakao.maps.MarkerImage(
        "https://t1.daumcdn.net/localimg/localimages/07/mapapidoc/markerStar.png",
        new kakao.maps.Size(34, 45)
      ),
      title: "내 위치",
    });

    // 반경 1km 표시
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

    // 마커 표시
    const nearbyPins = mockPins.filter(
      (p) => getDistance(currentLocation.lat, currentLocation.lng, p.latitude, p.longitude) <= 1
    );

    nearbyPins.forEach((pin) => {
      const marker = new kakao.maps.Marker({
        map,
        position: new kakao.maps.LatLng(pin.latitude, pin.longitude),
      });

      const info = new kakao.maps.InfoWindow({
        content: `<div style="padding:6px; font-size:12px;">📍 등록일: ${pin.createdAt.slice(0, 10)}</div>`,
      });

      kakao.maps.event.addListener(marker, "mouseover", () => info.open(map, marker));
      kakao.maps.event.addListener(marker, "mouseout", () => info.close());
      kakao.maps.event.addListener(marker, "click", () => setSelectedPin(pin));
    });
  }, [isMapLoaded, currentLocation]);

  // 1km 이내 + 검색 필터링
  const filteredPins = mockPins.filter((p) => {
    const dist = currentLocation
      ? getDistance(currentLocation.lat, currentLocation.lng, p.latitude, p.longitude)
      : 0;
    const match = p.post ? p.post.content.toLowerCase().includes(searchQuery.toLowerCase()) : false;
    return dist <= 1 && match;
  });

  // 리스트 클릭 시 지도 이동 + 모달 표시
  const handlePostClick = (pin: typeof mockPins[0]) => {
    if (mapInstance) {
      mapInstance.setCenter(new window.kakao.maps.LatLng(pin.latitude, pin.longitude));
    }
    setSelectedPin(pin);
  };

  // 핀 추가 버튼
  const handleAddPin = () => {
    setShowAddModal(true);
  };

  return (
    <div className="flex flex-col h-screen bg-gray-50">
      <Script
        src={`//dapi.kakao.com/v2/maps/sdk.js?appkey=${process.env.NEXT_PUBLIC_KAKAO_APP_KEY}&autoload=false`}
        strategy="afterInteractive"
      />

      {/* Body */}
      <main className="flex flex-1 overflow-hidden">
        {/* 왼쪽 리스트 */}
        <div className="bg-white border-r w-80 p-4 flex flex-col gap-4 overflow-y-auto">
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
            <h3 className="text-gray-700 font-semibold mb-2">📍 반경 1km 이내 게시글</h3>
            {filteredPins.length > 0 ? (
              filteredPins.map((pin) => (
                <div
                  key={pin.id}
                  onClick={() => handlePostClick(pin)}
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
          {!isMapLoaded && (
            <div className="absolute inset-0 flex items-center justify-center bg-white/80 text-gray-600">
              지도를 불러오는 중...
            </div>
          )}

          {/* 📍 핀 추가 버튼 */}
          <button
            className="absolute bottom-6 left-1/2 transform -translate-x-1/2 bg-blue-600 text-white px-5 py-3 rounded-lg shadow-lg hover:bg-blue-700 z-50 flex items-center gap-2"
            onClick={handleAddPin}
          >
            <Plus className="w-5 h-5" /> 핀 추가
          </button>

          {/* 🔍 확대/축소 컨트롤 */}
          <div className="absolute bottom-6 right-6 flex flex-col gap-3 z-50">
            <button
              className="bg-white border rounded-full shadow-md p-3 hover:bg-gray-100 flex items-center justify-center"
              onClick={() => mapInstance && mapInstance.setLevel(mapInstance.getLevel() + 1)}
              title="축소"
            >
              <ZoomOut className="w-5 h-5 text-gray-700" />
            </button>

            <button
              className="bg-white border rounded-full shadow-md p-3 hover:bg-gray-100 flex items-center justify-center"
              onClick={() => mapInstance && mapInstance.setLevel(mapInstance.getLevel() - 1)}
              title="확대"
            >
              <ZoomIn className="w-5 h-5 text-gray-700" />
            </button>
          </div>

          {/* 📍 게시글 모달 */}
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
                      <p className="text-gray-800 mb-6 leading-relaxed">{selectedPin.post.content}</p>
                      <div className="flex justify-between text-sm text-gray-500 border-t pt-3">
                        <span>작성일: {selectedPin.post.createdAt.slice(0, 10)}</span>
                        <span>수정일: {selectedPin.post.modifiedAt.slice(0, 10)}</span>
                      </div>
                    </>
                  ) : (
                    <p className="text-gray-500 text-center py-6">아직 게시글이 등록되지 않았습니다 🕓</p>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* 📍 핀 추가 후 게시물 작성 여부 모달 */}
          {showAddModal && (
            <div className="absolute inset-0 bg-black/50 flex items-center justify-center z-50">
              <div className="bg-white rounded-xl shadow-xl w-80 p-6 text-center animate-fadeIn">
                <h3 className="text-lg font-semibold mb-4">📍 핀 추가 완료!</h3>
                <p className="text-gray-700 mb-6">게시물도 작성하시겠습니까?</p>
                <div className="flex justify-center gap-4">
                  <button
                    className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700"
                    onClick={() => {
                      setShowAddModal(false);
                      setShowPostForm(true);
                    }}
                  >
                    예
                  </button>
                  <button
                    className="bg-gray-300 text-gray-800 px-4 py-2 rounded-md hover:bg-gray-400"
                    onClick={() => setShowAddModal(false)}
                  >
                    아니오
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* 📝 게시물 작성 폼 */}
          {showPostForm && (
            <div className="absolute inset-0 bg-black/50 flex items-center justify-center z-50">
              <div className="bg-white rounded-xl shadow-xl w-96 p-6 animate-fadeIn relative">
                <button
                  className="absolute top-3 right-3 text-gray-500 hover:text-black"
                  onClick={() => setShowPostForm(false)}
                >
                  <X className="w-5 h-5" />
                </button>
                <h2 className="text-lg font-semibold mb-4">📝 게시물 작성</h2>
                <textarea
                  className="w-full border rounded-md p-3 text-sm resize-none h-32 mb-4"
                  placeholder="게시글 내용을 입력하세요..."
                />
                <button
                  className="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700"
                  onClick={() => {
                    alert("게시물이 등록되었습니다!");
                    setShowPostForm(false);
                  }}
                >
                  등록하기
                </button>
              </div>
            </div>
          )}
        </div>
      </main>

      {/* 모달 애니메이션 */}
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
