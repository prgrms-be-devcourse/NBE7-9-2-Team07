import { useEffect, useRef } from "react";
import { PinDto } from "../types/types";

export function useKakaoMap({
  pins,
  center,
  onSelectPin,
}: {
  pins: PinDto[];
  center: { lat: number; lng: number };
  onSelectPin: (pin: PinDto) => void;
}) {
  const mapRef = useRef<any>(null);

  // init
  useEffect(() => {
    const kakao = (window as any).kakao;
    if (!kakao?.maps) return;

    const el = document.getElementById("map");
    if (!el) return;

    const map = new kakao.maps.Map(el, {
      center: new kakao.maps.LatLng(center.lat, center.lng),
      level: 4,
    });

    mapRef.current = map;
    (window as any).mapRef = map; // for simple controls
  }, []);

  // center move
  useEffect(() => {
    const kakao = (window as any).kakao;
    if (!kakao?.maps || !mapRef.current) return;
    mapRef.current.setCenter(new kakao.maps.LatLng(center.lat, center.lng));
  }, [center]);

  // markers + cluster
  useEffect(() => {
    const kakao = (window as any).kakao;
    const map = mapRef.current;
    if (!kakao?.maps || !map) return;

    if (map.markers) map.markers.forEach((m: any) => m.setMap(null));
    map.markers = [];

    const clusterer = new kakao.maps.MarkerClusterer({
      map,
      averageCenter: true,
      minLevel: 3,
      gridSize: 60,
    });

    const markers = pins.map(pin => {
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
