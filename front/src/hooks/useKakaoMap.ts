import { useEffect, useRef } from "react";
import { PinDto } from "../types/types";

export function useKakaoMap({
  pins,
  center,
  onSelectPin,
  kakaoReady,
  onCenterChange, // ✅ 중심 좌표 변경 콜백 추가
}: {
  pins: PinDto[];
  center: { lat: number; lng: number };
  onSelectPin: (pin: PinDto) => void;
  kakaoReady?: boolean;
  onCenterChange?: (lat: number, lng: number) => void; // ✅ 추가
}) {
  const mapRef = useRef<any>(null);
  const clustererRef = useRef<any>(null);
  const circleRef = useRef<any>(null);

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
    // 지도 이동/드래그 이벤트 리스너 등록
    kakao.maps.event.addListener(map, 'dragend', () => {
      const centerLatLng = map.getCenter();
      const newLat = centerLatLng.getLat();
      const newLng = centerLatLng.getLng();
      
      if (onCenterChange) {
        onCenterChange(newLat, newLng);
      }
    });

    // 지도 중심 좌표 변경 이벤트 (확대/축소 시에도 발생)
    kakao.maps.event.addListener(map, 'center_changed', () => {
      const centerLatLng = map.getCenter();
      const newLat = centerLatLng.getLat();
      const newLng = centerLatLng.getLng();
      
      if (onCenterChange) {
        onCenterChange(newLat, newLng);
      }
    });
    //=================
  }, [kakaoReady, onCenterChange]);

  // ✅ 중심 이동 + 반경 원 표시
  useEffect(() => {
    const kakao = (window as any).kakao;
    if (!kakao?.maps || !mapRef.current) return;

    const map = mapRef.current;
    const ll = new kakao.maps.LatLng(center.lat, center.lng);
    map.setCenter(ll);
/*
    // ✅ 1km 반경 원 표시 (중심 고정)
    if (circleRef.current) circleRef.current.setMap(null);
    const circle = new kakao.maps.Circle({
      center: ll,
      radius: 1000, // ✅ 1km
      strokeWeight: 2,
      strokeColor: "#2563EB",
      strokeOpacity: 0.8,
      strokeStyle: "solid",
      fillColor: "#60A5FA",
      fillOpacity: 0.15,
    });
    circle.setMap(map);
    circleRef.current = circle;
    */
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