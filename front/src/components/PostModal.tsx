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
  const [likeCount, setLikeCount] = useState(pin.likeCount ?? 0);
  const [isBookmarked, setIsBookmarked] = useState(false);
  const [isPublic, setIsPublic] = useState(pin.isPublic);
  const [newTag, setNewTag] = useState("");
  const [editing, setEditing] = useState(false);
  const [content, setContent] = useState(pin.content);

  /** ✅ 어떤 응답이 와도 태그 배열로 변환 */
  const parseTags = (resp: any): TagDto[] => {
    // 신구 구조 모두 대응
    if (Array.isArray(resp?.data?.tags)) return resp.data.tags as TagDto[];
    if (Array.isArray(resp?.data)) return resp.data as TagDto[];
    if (Array.isArray(resp)) return resp as TagDto[];
    return [];
  };

  // ✅ 초기 데이터 로드
  useEffect(() => {
    let mounted = true;

    (async () => {
      try {
        // 태그
        const t = await apiGetPinTags(pin.id);
        const parsedTags = parseTags(t);
        if (mounted) setTags(parsedTags);
      } catch (err) {
        console.error("태그 로드 실패:", err);
        if (mounted) setTags([]);
      }

      try {
        // 좋아요 유저
        const u = await apiGetLikeUsers(pin.id);
        const likeUserList = Array.isArray(u) ? u : u?.data ?? [];
        if (mounted) {
          setLikeUsers(likeUserList);
          setIsLiked(likeUserList.some((usr) => usr.id === userId));
          if (Array.isArray(likeUserList) && likeUserList.length !== likeCount) {
            setLikeCount(likeUserList.length);
          }
        }
      } catch (err) {
        console.error("좋아요 로드 실패:", err);
      }

      try {
        // 북마크 상태
        const res = await fetch(
          `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/bookmarks?userId=${userId}`
        );
        const data = await res.json();
        if (mounted) {
          if (data.errorCode === "200" && Array.isArray(data.data)) {
            setIsBookmarked(data.data.some((b: any) => b.pin?.id === pin.id));
          } else {
            setIsBookmarked(false);
          }
        }
      } catch {
        if (mounted) setIsBookmarked(false);
      }
    })();

    return () => {
      mounted = false;
    };
  }, [pin.id, userId]); // pin이 바뀌면 다시 로드

  // ✅ 태그 추가
  const addTag = async () => {
    if (!newTag.trim()) return;
    await apiAddTagToPin(pin.id, newTag.trim());
    const res = await apiGetPinTags(pin.id);
    setTags(parseTags(res)); // ✅ RsData.data.tags 대응
    setNewTag("");
    onChanged?.();
  };

  // ✅ 태그 제거
  const removeTag = async (tagId: number) => {
    await apiRemoveTagFromPin(pin.id, tagId);
    const res = await apiGetPinTags(pin.id);
    setTags(parseTags(res)); // ✅ 동일하게
    onChanged?.();
  };

  // ✅ 좋아요 토글 (낙관적 업데이트 + 보정)
  const toggleLike = async () => {
    try {
      setIsLiked((prev) => !prev);
      setLikeCount((prev) => (isLiked ? prev - 1 : prev + 1));

      const res = await apiToggleLike(pin.id, userId);

      if (res && typeof res.likeCount === "number") {
        setLikeCount(res.likeCount);
        setIsLiked(res.isLiked);
      } else {
        const updatedUsers = await apiGetLikeUsers(pin.id);
        const list = Array.isArray(updatedUsers)
          ? updatedUsers
          : updatedUsers?.data ?? [];
        setLikeUsers(list);
        setIsLiked(list.some((u) => u.id === userId));
        setLikeCount(list.length);
      }

      onChanged?.();
    } catch (err) {
      console.error("좋아요 토글 실패:", err);
      // 롤백
      setIsLiked((prev) => !prev);
      setLikeCount((prev) => (isLiked ? prev + 1 : prev - 1));
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
    }
  };

  // ✅ 공개 토글
  const togglePublic = async () => {
    try {
      // UI 즉시 반영
      setIsPublic((prev) => !prev);
      const res = await apiTogglePublic(pin.id);

      // 서버 응답 우선
      if (res?.data?.isPublic !== undefined) {
        setIsPublic(res.data.isPublic);
        alert(res.data.isPublic ? "🌍 공개로 전환되었습니다" : "🔒 비공개로 전환되었습니다");
      } else {
        alert(isPublic ? "🔒 비공개로 전환되었습니다" : "🌍 공개로 전환되었습니다");
      }

      onChanged?.();
    } catch (err) {
      console.error("공개 토글 실패:", err);
      // 롤백
      setIsPublic((prev) => !prev);
      alert("공개 설정 변경 중 오류가 발생했습니다.");
    }
  };

  // ✅ 내용 수정 저장
  const saveEdit = async () => {
    await apiUpdatePin(pin.id, pin.latitude, pin.longitude, content);
    setEditing(false);
    onChanged?.();
  };

  // ✅ 삭제
  const deletePin = async () => {
    if (!confirm("이 핀을 삭제할까요?")) return;
    await apiDeletePin(pin.id);
    onChanged?.();
    onClose();
  };

  return (
    <div className="absolute inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-xl shadow-xl w-[480px] max-w-[90%] relative animate-fadeIn">
        <button
          className="absolute top-3 right-3 text-gray-500 hover:text-black"
          onClick={onClose}
        >
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

          {/* ✅ 태그 섹션 */}
          <div className="mt-3">
            <div className="text-sm font-medium mb-2">🏷️ 태그</div>

            <div className="flex flex-wrap gap-2">
              {(!Array.isArray(tags) || tags.length === 0) && (
                <span className="text-xs text-gray-400">
                  등록된 태그 없음
                </span>
              )}

              {Array.isArray(tags) &&
                tags.map((t) => (
                  <span
                    key={t.id}
                    className="px-2 py-1 text-xs border rounded-full bg-gray-50 flex items-center gap-1"
                  >
                    #{t.keyword}
                    {editing && (
                      <button
                        onClick={() => removeTag(t.id)}
                        className="text-red-500 hover:text-red-700 text-xs"
                      >
                        ×
                      </button>
                    )}
                  </span>
                ))}
            </div>

            {editing && (
              <div className="mt-2 flex gap-2">
                <input
                  value={newTag}
                  onChange={(e) => setNewTag(e.target.value)}
                  placeholder="새 태그 입력"
                  className="flex-1 border rounded-md px-2 py-1 text-sm"
                />
                <button
                  onClick={addTag}
                  className="px-3 py-1 rounded-md bg-blue-600 text-white text-sm"
                >
                  추가
                </button>
              </div>
            )}
          </div>

          {/* ✅ 컨트롤 버튼 */}
          <div className="flex flex-wrap gap-2">
            {editing ? (
              <>
                <button
                  onClick={saveEdit}
                  className="px-3 py-1 rounded-md bg-blue-600 text-white"
                >
                  저장
                </button>
                <button
                  onClick={() => setEditing(false)}
                  className="px-3 py-1 rounded-md border text-gray-600"
                >
                  취소
                </button>
              </>
            ) : (
              <>
                <button
                  onClick={toggleLike}
                  className={`px-3 py-1 rounded-md border transition ${isLiked
                    ? "bg-red-100 text-red-600 border-red-300"
                    : "border-gray-300"
                    }`}
                >
                  {isLiked ? "💔 좋아요 취소" : "👍 좋아요"} ({likeCount})
                </button>

                <button
                  onClick={togglePublic}
                  className={`px-3 py-1 rounded-md border transition ${isPublic
                    ? "bg-green-100 text-green-700 border-green-400 hover:bg-green-200"
                    : "bg-gray-100 text-gray-700 border-gray-300 hover:bg-gray-200"
                    }`}
                >
                  {isPublic ? "🔓 공개 중" : "🔒 비공개"}
                </button>

                <button
                  onClick={toggleBookmark}
                  className={`px-3 py-1 rounded-md border transition ${isBookmarked
                    ? "bg-blue-100 text-blue-600 border-blue-300"
                    : "border-gray-300"
                    }`}
                >
                  {isBookmarked ? "🔖 북마크됨" : "📌 북마크"}
                </button>

                <button
                  onClick={() => setEditing(true)}
                  className="px-3 py-1 rounded-md border"
                >
                  ✏️ 편집
                </button>

                <button
                  onClick={deletePin}
                  className="px-3 py-1 rounded-md border text-red-600"
                >
                  🗑 삭제
                </button>
              </>
            )}
          </div>

          <div className="text-sm">
            <span className="font-medium">좋아요한 유저:</span>{" "}
            {likeUsers.length
              ? likeUsers.map((u) => u.userName).join(", ")
              : "없음"}
          </div>
        </div>
      </div>
    </div>
  );
}
