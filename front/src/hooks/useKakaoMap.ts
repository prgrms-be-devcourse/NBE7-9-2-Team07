import { useEffect, useRef } from "react";
import { PinDto } from "../types/types";

export function useKakaoMap({
  pins,
  center,
  onSelectPin,
  kakaoReady,
  onCenterChange,
}: {
  pins: PinDto[];
  center: { lat: number; lng: number };
  onSelectPin: (pin: PinDto) => void;
  kakaoReady?: boolean;
  onCenterChange?: (lat: number, lng: number) => void;
}) {
  const mapRef = useRef<any>(null);
  const clustererRef = useRef<any>(null);
  const circleRef = useRef<any>(null);
  //=================
  // 디바운스를 위한 타이머 ref
  const debounceTimerRef = useRef<NodeJS.Timeout | null>(null);
  // 마지막으로 업데이트된 좌표 저장 (불필요한 업데이트 방지)
  const lastCenterRef = useRef({ lat: center.lat, lng: center.lng });
  //=================

  // ✅ 지도 초기화 (kakaoReady 이후에만)
  useEffect(() => {
    if (!kakaoReady) return;

    const kakao = (window as any).kakao;
    if (!kakao?.maps) return;

    const el = document.getElementById("map");
    if (!el) return;

    const map = new kakao.maps.Map(el, {
      center: new kakao.maps.LatLng(center.lat, center.lng),
      level: 4,
    });

    mapRef.current = map;
    (window as any).mapRef = map;

    //=================
    // 🚀 최적화 1: dragend만 사용 (center_changed 제거)
    // 🚀 최적화 2: 디바운스 적용 (500ms 대기)
    kakao.maps.event.addListener(map, 'dragend', () => {
      // 기존 타이머 취소
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }

      // 새 타이머 설정 (500ms 후 실행)
      debounceTimerRef.current = setTimeout(() => {
        const centerLatLng = map.getCenter();
        const newLat = centerLatLng.getLat();
        const newLng = centerLatLng.getLng();

        // 🚀 최적화 3: 좌표가 실제로 변경되었을 때만 업데이트
        const latDiff = Math.abs(newLat - lastCenterRef.current.lat);
        const lngDiff = Math.abs(newLng - lastCenterRef.current.lng);
        
        // 0.0001도 이상 변경되었을 때만 업데이트 (약 10m)
        if (latDiff > 0.0001 || lngDiff > 0.0001) {
          console.log("🗺️ 지도 중심 업데이트:", { lat: newLat, lng: newLng });
          lastCenterRef.current = { lat: newLat, lng: newLng };
          
          if (onCenterChange) {
            onCenterChange(newLat, newLng);
          }
        }
      }, 500); // 500ms 디바운스
    });
    //=================

    // 클린업
    return () => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }
    };
  }, [kakaoReady, onCenterChange]);

  // ✅ 중심 이동 + 반경 원 표시
  useEffect(() => {
    const kakao = (window as any).kakao;
    if (!kakao?.maps || !mapRef.current) return;

    const map = mapRef.current;
    const ll = new kakao.maps.LatLng(center.lat, center.lng);
    
    //=================
    // 🚀 최적화 4: 중심 좌표 변경이 의미있을 때만 setCenter 호출
    const currentCenter = map.getCenter();
    const latDiff = Math.abs(currentCenter.getLat() - center.lat);
    const lngDiff = Math.abs(currentCenter.getLng() - center.lng);
    
    if (latDiff > 0.0001 || lngDiff > 0.0001) {
      map.setCenter(ll);
    }
    //=================

    // ✅ 1km 반경 원 표시 (중심 고정)
    if (circleRef.current) circleRef.current.setMap(null);
    const circle = new kakao.maps.Circle({
      center: ll,
      radius: 1000,
      strokeWeight: 2,
      strokeColor: "#2563EB",
      strokeOpacity: 0.8,
      strokeStyle: "solid",
      fillColor: "#60A5FA",
      fillOpacity: 0.15,
    });
    circle.setMap(map);
    circleRef.current = circle;
  }, [center, kakaoReady]);

  // ✅ 마커 및 클러스터러 관리
  useEffect(() => {
    const kakao = (window as any).kakao;
    const map = mapRef.current;
    if (!kakao?.maps || !map) return;

    if (clustererRef.current) clustererRef.current.clear();

    const clusterer = new kakao.maps.MarkerClusterer({
      map,
      averageCenter: true,
      minLevel: 3,
      gridSize: 60,
      //=================
      // 🚀 최적화 5: 클러스터러 성능 옵션 추가
      disableClickZoom: false,
      calculator: [10, 30, 50], // 클러스터 크기 임계값
      //=================
    });

    const markers = pins.map((pin) => {
      const marker = new kakao.maps.Marker({
        position: new kakao.maps.LatLng(pin.latitude, pin.longitude),
      });
      kakao.maps.event.addListener(marker, "click", () => onSelectPin(pin));
      return marker;
    });

    clusterer.addMarkers(markers);
    clustererRef.current = clusterer;

    return () => {
      clusterer.clear();
      markers.forEach((m) => m.setMap(null));
    };
  }, [pins, onSelectPin, kakaoReady]);

  return { map: mapRef.current };
}