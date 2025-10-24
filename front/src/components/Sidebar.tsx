"use client";

import { TagDto, PinDto } from "@/hooks/usePins";
import { Loader2, MapPin, Star, Heart, Compass, Globe, X } from "lucide-react";

interface SidebarProps {
  pins: PinDto[];
  loading: boolean;
  mode: string;
  allTags: TagDto[];
  selectedTags: string[];
  onChangeTags: (next: string[]) => void;
  onClickAll: () => void;
  onClickNearBy: () => void;
  onClickMyBookmarks: () => void;
  onClickLikedPins: () => void;
  onSelectPin: (pin: PinDto) => void;
}

export default function Sidebar({
  pins,
  loading,
  mode,
  allTags,
  selectedTags,
  onChangeTags,
  onClickAll,
  onClickNearBy,
  onClickMyBookmarks,
  onClickLikedPins,
  onSelectPin,
}: SidebarProps) {
  // ✅ 전체 보기 클릭 시 태그 상태도 초기화
  const handleClickAll = () => {
    if (selectedTags.length > 0) onChangeTags([]); // 태그 선택 해제
    onClickAll();
  };

    const handleClickNearBy = () => {
        if (selectedTags.length > 0) onChangeTags([]); // 태그 선택 해제
        onClickNearBy(); // 전체 보기 로직 실행
    };

  // ✅ 전체 해제 버튼 클릭 시 태그 해제
  const handleClearTags = () => {
    onChangeTags([]);
  };

  return (
    <aside className="w-80 bg-white border-r flex flex-col overflow-hidden">

      {/* 필터 버튼 영역 */}
      <div className="p-3 border-b flex flex-wrap gap-2">
          <button
              onClick={handleClickAll}
              className={`flex items-center gap-1 px-3 py-1.5 rounded-md text-sm font-medium ${mode === "screen" ? "bg-green-600 text-white" : "bg-gray-100 hover:bg-gray-200"
              }`}
          >
              <Globe size={16} /> 전체 보기
          </button>

        <button
          onClick={handleClickNearBy}
          className={`flex items-center gap-1 px-3 py-1.5 rounded-md text-sm font-medium ${mode === "nearby" ? "bg-blue-600 text-white" : "bg-gray-100 hover:bg-gray-200"
            }`}
        >
          <Globe size={16} /> 내 주변 보기
        </button>


        <button
          onClick={onClickLikedPins}
          className={`flex items-center gap-1 px-3 py-1.5 rounded-md text-sm font-medium ${mode === "liked" ? "bg-pink-600 text-white" : "bg-gray-100 hover:bg-gray-200"
            }`}
        >
          <Heart size={16} /> 좋아요한 핀
        </button>

        <button
          onClick={onClickMyBookmarks}
          className={`flex items-center gap-1 px-3 py-1.5 rounded-md text-sm font-medium ${mode === "bookmark" ? "bg-yellow-400 text-gray-800" : "bg-gray-100 hover:bg-gray-200"
            }`}
        >
          <Star size={16} /> 내 북마크
        </button>
      </div>

      {/* 태그 필터 섹션 */}
      <div className="p-3 border-b">
        <div className="flex items-center justify-between mb-2">
          <h3 className="text-sm font-medium text-gray-600">🧩 태그 필터</h3>
          {selectedTags.length > 0 && (
            <button
              onClick={handleClearTags}
              className="flex items-center gap-1 text-xs text-gray-500 hover:text-black"
            >
              <X size={12} /> 전체 해제
            </button>
          )}
        </div>

        <div className="flex flex-wrap gap-2">
          {allTags.length === 0 && <p className="text-xs text-gray-400">불러오는 중...</p>}
          {allTags.map((tag) => (
            <button
              key={tag.id}
              onClick={() =>
                onChangeTags(
                  selectedTags.includes(tag.keyword)
                    ? selectedTags.filter((t) => t !== tag.keyword)
                    : [...selectedTags, tag.keyword]
                )
              }
              className={`px-2 py-1 rounded-md text-xs border transition ${selectedTags.includes(tag.keyword)
                  ? "bg-blue-600 text-white border-blue-600"
                  : "bg-gray-100 hover:bg-gray-200 border-gray-300"
                }`}
            >
              #{tag.keyword}
            </button>
          ))}
        </div>
      </div>

      {/* 핀 목록 */}
      <div className="flex-1 overflow-y-auto p-4">
        {loading ? (
          <div className="flex flex-col items-center justify-center text-gray-500 mt-10">
            <Loader2 className="animate-spin mb-2" size={20} />
            <p className="text-sm">불러오는 중...</p>
          </div>
        ) : pins.length === 0 ? (
          <p className="text-sm text-gray-400 text-center mt-10">표시할 핀이 없습니다 💤</p>
        ) : (
          <ul className="space-y-2">
            {pins.map((pin) => (
              <li
                key={pin.id}
                onClick={() => onSelectPin(pin)}
                className="p-3 bg-gray-50 rounded-lg hover:bg-blue-50 cursor-pointer border border-gray-200 transition"
              >
                <div className="flex items-start justify-between">
                  <p className="text-sm text-gray-700 line-clamp-2">{pin.content}</p>
                  <span className="text-xs text-gray-400">{pin.likeCount ?? 0} ❤️</span>
                </div>
                <div className="flex items-center gap-1 mt-1 text-xs text-gray-400">
                  <MapPin size={12} /> {pin.latitude.toFixed(4)}, {pin.longitude.toFixed(4)}
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </aside>
  );
}
