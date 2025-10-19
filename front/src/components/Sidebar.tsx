import { useMemo, useState } from "react";
import { Search } from "lucide-react";
import { PinDto, TagDto } from "../types/types";
import TagFilter from "./TagFilter";
import PostCard from "./PostCard";

type Props = {
  pins: PinDto[];
  loading: boolean;
  mode: "all" | "nearby" | "tag" | "bookmark";

  allTags: TagDto[];
  selectedTags: string[];
  onChangeTags: (next: string[]) => void;

  onClickNearby: () => void;
  onClickAll: () => void;
  onClickMyBookmarks: () => void;

  onSelectPin: (pin: PinDto) => void;
};

export default function Sidebar({
  pins,
  loading,
  mode,
  allTags,
  selectedTags,
  onChangeTags,
  onClickNearby,
  onClickAll,
  onClickMyBookmarks,
  onSelectPin,
}: Props) {
  const [search, setSearch] = useState("");

  const filtered = useMemo(() => {
    const q = search.toLowerCase();
    return pins.filter((p) => p.content.toLowerCase().includes(q));
  }, [pins, search]);

  return (
    <aside className="bg-white border-r w-80 p-4 flex flex-col gap-4 overflow-y-auto">
      <div className="flex items-center justify-between">
        <h3 className="text-gray-800 font-semibold">📍 핀 목록</h3>
        <div className="flex gap-2">
          <button
            onClick={onClickNearby}
            className={`px-2 py-1 text-xs rounded-md ${mode === "nearby" ? "bg-blue-600 text-white" : "bg-gray-100 text-gray-700"}`}
          >
            주변 보기
          </button>
          <button
            onClick={onClickAll}
            className={`px-2 py-1 text-xs rounded-md ${mode === "all" ? "bg-blue-600 text-white" : "bg-gray-100 text-gray-700"}`}
          >
            모두 보기
          </button>
        </div>
      </div>

      <TagFilter
        allTags={allTags}
        selectedTags={selectedTags}
        onChange={onChangeTags}
        onBookmarkClick={onClickMyBookmarks}
        currentMode={mode}
      />

      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
        <input
          type="text"
          placeholder="게시글 검색..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="pl-10 w-full border rounded-md p-2 text-sm"
        />
      </div>

      <div className="space-y-2">
        {loading ? (
          <p className="text-gray-400 text-sm text-center py-6">불러오는 중입니다... ⏳</p>
        ) : filtered.length > 0 ? (
          filtered.map((pin) => <PostCard key={pin.id} pin={pin} onClick={() => onSelectPin(pin)} />)
        ) : (
          <p className="text-gray-400 text-sm text-center py-6">게시글이 없습니다 😢</p>
        )}
      </div>
    </aside>
  );
}
