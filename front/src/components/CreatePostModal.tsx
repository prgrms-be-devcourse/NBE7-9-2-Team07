"use client";

import { useEffect, useState } from "react";
import { apiCreatePin, apiAddTagToPin, apiGetAllTags } from "@/lib/pincoApi";
import { TagDto } from "@/types/types";

export default function CreatePostModal({
  lat,
  lng,
  userId = 1,
  onClose,
  onCreated,
}: {
  lat: number;
  lng: number;
  userId?: number;
  onClose: () => void;
  onCreated?: () => void;
}) {
  const [content, setContent] = useState("");
  const [allTags, setAllTags] = useState<TagDto[]>([]);
  const [selectedTags, setSelectedTags] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);

  // ✅ 태그 불러오기
  useEffect(() => {
    const fetchTags = async () => {
      try {
        const tags = await apiGetAllTags();
        setAllTags(Array.isArray(tags) ? tags : []);
      } catch (err) {
        console.error("태그 목록 불러오기 실패:", err);
        setAllTags([]);
      }
    };
    fetchTags();
  }, []);

  // ✅ 태그 토글
  const toggleTag = (keyword: string) => {
    setSelectedTags((prev) =>
      prev.includes(keyword)
        ? prev.filter((t) => t !== keyword)
        : [...prev, keyword]
    );
  };

  // ✅ 게시글 등록
  const handleSubmit = async () => {
    if (!content.trim()) {
      alert("내용을 입력해주세요.");
      return;
    }

    setLoading(true);
    try {
      const pin = await apiCreatePin(lat, lng, content); // ✅ pin.id 바로 있음

      if (selectedTags.length > 0) {
        await Promise.all(
          selectedTags.map((kw) => apiAddTagToPin(pin.id, kw))
        );
      }

      alert("게시글이 등록되었습니다 🎉");
      onCreated?.();
      onClose();
    } catch (err) {
      console.error("❌ 게시글 등록 실패:", err);
      alert("게시글 등록 중 오류가 발생했습니다 ❌");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="absolute inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-xl shadow-xl w-[480px] max-w-[90%] relative animate-fadeIn p-6">
        <button
          className="absolute top-3 right-3 text-gray-500 hover:text-black"
          onClick={onClose}
        >
          ✕
        </button>

        <h2 className="text-lg font-semibold mb-3">📝 새 게시글 작성</h2>

        {/* 게시글 입력 */}
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="게시글 내용을 입력하세요..."
          className="w-full border rounded-md p-2 h-32 text-sm resize-none mb-4"
        />

        {/* 태그 버튼 리스트 */}
        <div>
          <label className="block text-sm font-medium mb-1">🏷️ 태그 선택</label>

          {allTags.length === 0 ? (
            <p className="text-sm text-gray-400">등록된 태그가 없습니다.</p>
          ) : (
            <div className="flex flex-wrap gap-2">
              {allTags.map((tag) => {
                const selected = selectedTags.includes(tag.keyword);
                return (
                  <button
                    key={tag.id}
                    type="button"
                    onClick={() => toggleTag(tag.keyword)}
                    className={`px-3 py-1 rounded-full text-sm border transition ${selected
                      ? "bg-blue-600 text-white border-blue-600"
                      : "bg-gray-100 text-gray-700 border-gray-300 hover:bg-gray-200"
                      }`}
                  >
                    #{tag.keyword}
                  </button>
                );
              })}
            </div>
          )}
        </div>

        <button
          onClick={handleSubmit}
          disabled={loading}
          className={`w-full bg-blue-600 text-white py-2 rounded-md mt-6 hover:bg-blue-700 transition ${loading ? "opacity-70 cursor-not-allowed" : ""
            }`}
        >
          {loading ? "등록 중..." : "등록하기"}
        </button>
      </div>
    </div>
  );
}
