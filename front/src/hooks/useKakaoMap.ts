import { useEffect, useRef } from "react";
import { PinDto } from "../types/types";

type Props = {
  pins: PinDto[];
  center: { lat: number; lng: number };
  onSelectPin: (pin: PinDto) => void;
};

export function useKakaoMap({ pins, center, onSelectPin }: Props) {
  const mapRef = useRef<any>(null);

  // 지도 초기화
  useEffect(() => {
    const kakao = (window as any).kakao;
    if (!kakao?.maps) return;

    const container = document.getElementById("map");
    if (!container) return;

    const map = new kakao.maps.Map(container, {
      center: new kakao.maps.LatLng(center.lat, center.lng),
      level: 4,
    });

    mapRef.current = map;
  }, []);

  // 지도 중심 이동
  useEffect(() => {
    const kakao = (window as any).kakao;
    if (!kakao?.maps || !mapRef.current) return;
    const move = new kakao.maps.LatLng(center.lat, center.lng);
    mapRef.current.setCenter(move);
  }, [center]);

  // 마커/클러스터
  useEffect(() => {
    const kakao = (window as any).kakao;
    const map = mapRef.current;
    if (!kakao?.maps || !map) return;

    // 기존 마커 정리
    if (map.markers) map.markers.forEach((m: any) => m.setMap(null));
    map.markers = [];

    // 클러스터러
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
    map.markers = markers;
  }, [pins, onSelectPin]);

  return { map: mapRef.current };
}
