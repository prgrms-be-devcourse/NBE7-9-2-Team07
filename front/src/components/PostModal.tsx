import { useEffect, useState } from "react";
import { X } from "lucide-react";
import { PinDto, PinLikedUserDto, TagDto } from "../types/types";
import {
  apiAddTagToPin,
  apiDeleteBookmark,
  apiDeletePin,
  apiGetLikeUsers,
  apiGetPinTags,
  apiToggleLike,
  apiTogglePublic,
  apiUpdatePin,
  apiCreateBookmark,
  apiRemoveTagFromPin,
} from "../lib/pincoApi";

export default function PostModal({
  pin,
  onClose,
  userId = 1,
  onChanged,
}: {
  pin: PinDto;
  onClose: () => void;
  userId?: number;
  onChanged?: () => void;
}) {
  const [tags, setTags] = useState<TagDto[]>([]);
  const [likeUsers, setLikeUsers] = useState<PinLikedUserDto[]>([]);
  const [isLiked, setIsLiked] = useState(false);
  const [isBookmarked, setIsBookmarked] = useState(false);
  const [newTag, setNewTag] = useState("");
  const [editing, setEditing] = useState(false);
  const [content, setContent] = useState(pin.content);

  // ✅ 초기 데이터 로드
  useEffect(() => {
    (async () => {
      try {
        // --- 태그 ---
        const t = await apiGetPinTags(pin.id);
        if (Array.isArray(t)) setTags(t);
        else if (t?.data && Array.isArray(t.data)) setTags(t.data);
        else setTags([]);
      } catch (err) {
        console.error("태그 로드 실패:", err);
        setTags([]);
      }

      try {
        // --- 좋아요 유저 목록 ---
        const u = await apiGetLikeUsers(pin.id);
        if (Array.isArray(u)) {
          setLikeUsers(u);
          setIsLiked(u.some((usr) => usr.id === userId));
        } else if (u?.data && Array.isArray(u.data)) {
          setLikeUsers(u.data);
          setIsLiked(u.data.some((usr) => usr.id === userId));
        } else {
          setLikeUsers([]);
          setIsLiked(false);
        }
      } catch (err) {
        console.error("좋아요 로드 실패:", err);
        setLikeUsers([]);
        setIsLiked(false);
      }

      try {
        // --- 북마크 상태 ---
        const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/bookmarks?userId=${userId}`);
        const data = await res.json();
        if (data.errorCode === "200" && Array.isArray(data.data)) {
          setIsBookmarked(data.data.some((b: any) => b.pin?.id === pin.id));
        } else {
          setIsBookmarked(false);
        }
      } catch (err) {
        console.error("북마크 로드 실패:", err);
        setIsBookmarked(false);
      }
    })();
  }, [pin.id, userId]);

  // ✅ 태그 추가
  const addTag = async () => {
    if (!newTag.trim()) return;
    try {
      await apiAddTagToPin(pin.id, newTag.trim());
      const t = await apiGetPinTags(pin.id);
      setTags(Array.isArray(t) ? t : t?.data ?? []);
      setNewTag("");
      onChanged?.();
    } catch (err) {
      console.error("태그 추가 실패:", err);
      alert("태그 추가 중 문제가 발생했습니다.");
    }
  };

  // ✅ 태그 제거
  const removeTag = async (tagId: number) => {
    try {
      await apiRemoveTagFromPin(pin.id, tagId);
      const t = await apiGetPinTags(pin.id);
      setTags(Array.isArray(t) ? t : t?.data ?? []);
      onChanged?.();
    } catch (err) {
      console.error("태그 제거 실패:", err);
      alert("태그 제거 중 오류가 발생했습니다.");
    }
  };

  // ✅ 좋아요 토글
  const toggleLike = async () => {
    try {
      await apiToggleLike(pin.id, userId);
      const updated = await apiGetLikeUsers(pin.id);
      const arr = Array.isArray(updated) ? updated : updated?.data ?? [];
      setLikeUsers(arr);
      setIsLiked(arr.some((u) => u.id === userId));
      onChanged?.();
    } catch (err) {
      console.error("좋아요 토글 실패:", err);
      alert("좋아요 처리 중 오류가 발생했습니다.");
    }
  };

  // ✅ 북마크 토글
  const toggleBookmark = async () => {
    try {
      if (isBookmarked) {
        await apiDeleteBookmark(pin.id, userId);
        setIsBookmarked(false);
      } else {
        await apiCreateBookmark(userId, pin.id);
        setIsBookmarked(true);
        alert("북마크되었습니다 ✅");
      }
      onChanged?.();
    } catch (err) {
      console.error("북마크 토글 실패:", err);
      alert("북마크 처리 중 오류가 발생했습니다.");
    }
  };

  // ✅ 공개 토글
  const togglePublic = async () => {
    try {
      await apiTogglePublic(pin.id);
      onChanged?.();
    } catch (err) {
      console.error("공개 토글 실패:", err);
      alert("공개 설정 변경 중 오류가 발생했습니다.");
    }
  };

  // ✅ 게시글 수정 저장
  const saveEdit = async () => {
    try {
      await apiUpdatePin(pin.id, pin.latitude, pin.longitude, content);
      setEditing(false);
      onChanged?.();
    } catch (err) {
      console.error("게시글 수정 실패:", err);
      alert("게시글 수정 중 오류가 발생했습니다.");
    }
  };

  // ✅ 게시글 삭제
  const deletePin = async () => {
    if (!confirm("이 핀을 삭제할까요?")) return;
    try {
      await apiDeletePin(pin.id);
      onChanged?.();
      onClose();
    } catch (err) {
      console.error("핀 삭제 실패:", err);
      alert("핀 삭제 중 오류가 발생했습니다.");
    }
  };

  return (
    <div className="absolute inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-xl shadow-xl w-[480px] max-w-[90%] relative animate-fadeIn">
        <button className="absolute top-3 right-3 text-gray-500 hover:text-black" onClick={onClose}>
          <X className="w-5 h-5" />
        </button>

        <div className="p-6 space-y-4">
          <h2 className="text-lg font-semibold">📝 게시글</h2>

          {editing ? (
            <textarea
              className="w-full border rounded-md p-2 h-32 text-sm"
              value={content}
              onChange={(e) => setContent(e.target.value)}
            />
          ) : (
            <p className="text-gray-800 leading-relaxed">{pin.content}</p>
          )}

          <div className="text-xs text-gray-500 flex justify-between">
            <span>작성: {pin.createdAt.slice(0, 10)}</span>
            <span>수정: {pin.modifiedAt.slice(0, 10)}</span>
          </div>

          {/* ✅ 태그 목록 안전 렌더링 */}
          <div>
            <div className="text-sm font-medium mb-2">태그</div>
            <div className="flex gap-2 flex-wrap">
              {Array.isArray(tags) && tags.length > 0 ? (
                tags.map((t) => (
                  <span key={t.id} className="px-2 py-1 text-xs rounded-full bg-gray-100 border border-gray-200">
                    #{t.keyword}
                    <button className="ml-2 text-red-500" onClick={() => removeTag(t.id)}>
                      삭제
                    </button>
                  </span>
                ))
              ) : (
                <p className="text-xs text-gray-400">등록된 태그 없음</p>
              )}
            </div>
            <div className="mt-2 flex gap-2">
              <input
                placeholder="새 태그..."
                value={newTag}
                onChange={(e) => setNewTag(e.target.value)}
                className="flex-1 border rounded-md px-2 py-1 text-sm"
              />
              <button onClick={addTag} className="px-3 py-1 text-sm rounded-md bg-blue-600 text-white">
                추가
              </button>
            </div>
          </div>

          {/* ✅ 컨트롤 버튼 */}
          <div className="flex flex-wrap gap-2">
            <button
              onClick={toggleLike}
              className={`px-3 py-1 rounded-md border ${isLiked ? "bg-red-100 text-red-600" : ""}`}
            >
              👍 좋아요 ({likeUsers?.length ?? 0})
            </button>
            <button onClick={togglePublic} className="px-3 py-1 rounded-md border">
              🔁 공개 토글
            </button>
            <button
              onClick={toggleBookmark}
              className={`px-3 py-1 rounded-md border ${isBookmarked ? "bg-blue-100 text-blue-600" : ""}`}
            >
              🔖 {isBookmarked ? "북마크됨" : "북마크"}
            </button>
            {editing ? (
              <button onClick={saveEdit} className="px-3 py-1 rounded-md bg-blue-600 text-white">
                저장
              </button>
            ) : (
              <button onClick={() => setEditing(true)} className="px-3 py-1 rounded-md border">
                ✏️ 편집
              </button>
            )}
            <button onClick={deletePin} className="px-3 py-1 rounded-md border text-red-600">
              🗑 삭제
            </button>
          </div>

          {/* ✅ 좋아요 유저 목록 */}
          <div className="text-sm">
            <span className="font-medium">좋아요한 유저:</span>{" "}
            {Array.isArray(likeUsers) && likeUsers.length > 0
              ? likeUsers.map((u) => u.userName).join(", ")
              : "없음"}
          </div>
        </div>
      </div>
    </div>
  );
}
