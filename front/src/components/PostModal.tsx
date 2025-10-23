"use client";

import { useEffect, useState } from "react";
import { X } from "lucide-react";
import { PinDto, PinLikedUserDto, TagDto } from "../types/types";
import {
  apiAddTagToPin,
  apiDeleteBookmark,
  apiDeletePin,
  apiGetLikeUsers,
  apiGetPinTags,
  apiAddLike,
  apiRemoveLike,
  apiTogglePublic,
  apiUpdatePin,
  apiCreateBookmark,
  apiRemoveTagFromPin,
  apiGetMyBookmarks,
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
  const [bookmarkId, setBookmarkId] = useState<number | null>(null);
  const [newTag, setNewTag] = useState("");
  const [editing, setEditing] = useState(false);
  const [content, setContent] = useState(pin.content);
  const [currentPin, setCurrentPin] = useState(pin);

  // pin이 바뀌면 모달 내부도 동기화 (content까지)
  useEffect(() => {
    setCurrentPin(pin);
    setContent(pin.content);
  }, [pin.id, pin.content]);

  // ✅ 공개 상태는 즉시 반영 위해 로컬 상태 따로 유지
  const [localPublic, setLocalPublic] = useState(pin.isPublic);
  useEffect(() => {
    setLocalPublic(pin.isPublic);
  }, [pin.isPublic]);

  /** ✅ 어떤 응답이 와도 태그 배열로 변환 */
  const parseTags = (resp: any): TagDto[] => {
    if (Array.isArray(resp?.data?.tags)) return resp.data.tags as TagDto[];
    if (Array.isArray(resp?.data)) return resp.data as TagDto[];
    if (Array.isArray(resp)) return resp as TagDto[];
    return [];
  };

  // ✅ 초기 데이터 로드
  useEffect(() => {
    let mounted = true;

    const loadData = async () => {
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
          setLikeCount(likeUserList.length);
        }
      } catch (err) {
        console.error("좋아요 로드 실패:", err);
      }

      // ✅ 북마크 상태 불러오기
      try {
        const myBookmarks = await apiGetMyBookmarks();

        if (mounted && Array.isArray(myBookmarks)) {
          const found = myBookmarks.find((b: any) => b.pin?.id === pin.id);
          if (found) {
            setIsBookmarked(true);
            setBookmarkId(found.id); // ✅ bookmarkId 저장
          } else {
            setIsBookmarked(false);
            setBookmarkId(null);
          }
        } else if (mounted) {
          setIsBookmarked(false);
          setBookmarkId(null);
        }
      } catch (err) {
        console.error("북마크 로드 실패:", err);
        if (mounted) {
          setIsBookmarked(false);
          setBookmarkId(null);
        }
      }
    };

    loadData();

    return () => {
      mounted = false;
    };
  }, [pin.id, userId]);

  // ✅ 태그 추가
  const addTag = async () => {
    if (!newTag.trim()) return;
    await apiAddTagToPin(pin.id, newTag.trim());
    const res = await apiGetPinTags(pin.id);
    setTags(parseTags(res));
    setNewTag("");
    onChanged?.();
  };

  // ✅ 태그 제거
  const removeTag = async (tagId: number) => {
    await apiRemoveTagFromPin(pin.id, tagId);
    const res = await apiGetPinTags(pin.id);
    setTags(parseTags(res));
    onChanged?.();
  };

  // ✅ 좋아요 토글
  const toggleLike = async () => {
    try {
      let res;
      if (!isLiked) {
        res = await apiAddLike(pin.id, userId);
      } else {
        res = await apiRemoveLike(pin.id, userId);
      }

      const updated = res?.data;
      if (updated) {
        setIsLiked(updated.isLiked);
        setLikeCount(updated.likeCount);
      }

      onChanged?.({ ...pin, likeCount: updated?.likeCount ?? likeCount });
    } catch (err) {
      console.error("좋아요 요청 실패:", err);
    }
  };

  // ✅ 북마크 토글
  const toggleBookmark = async () => {
    try {
      if (isBookmarked && bookmarkId) {
        await apiDeleteBookmark(bookmarkId);
        setIsBookmarked(false);
        setBookmarkId(null);
        console.log("🔖 북마크 해제 완료");
      } else {
        const newBookmark = await apiCreateBookmark(pin.id);

        if (newBookmark) {
          setBookmarkId(newBookmark.id);
          setIsBookmarked(true);
          console.log("📌 북마크 생성 완료");
        }
      }

      onChanged?.();
    } catch (err) {
      console.error("북마크 토글 실패:", err);
    }
  };

  // ✅ 공개 토글
  const togglePublic = async () => {
      if (!userId) {
          alert("로그인 후 이용 가능합니다.");
          return;
      } else if (userId != pin.userId) {
          alert("수정 권한이 없습니다.");
          return;
      }
    const next = !localPublic;
    setLocalPublic(next);

    try {
      const res = await apiTogglePublic(pin.id);
      const updatedPin =
        res?.data && res.data.isPublic !== undefined ? res.data : res;
      const confirmed = updatedPin?.isPublic ?? next;
      setLocalPublic(confirmed);

      alert(
        confirmed ? "🌍 공개로 전환되었습니다" : "🔒 비공개로 전환되었습니다"
      );

      await onChanged?.();
    } catch (err) {
      console.error("공개 토글 실패:", err);
      setLocalPublic(!next);
      alert("공개 설정 변경 중 오류가 발생했습니다.");
    }
  };

  // ✅ 내용 수정 저장
  const saveEdit = async () => {
      if (!userId) {
          alert("로그인 후 이용 가능합니다.");
          return;
      } else if (userId != pin.userId) {
          alert("수정 권한이 없습니다.");
          return;
      }
      try {
          await apiUpdatePin(currentPin.id, currentPin.latitude, currentPin.longitude, content);

          // 서버에서 최신 핀 가져오기
          const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/pins/${currentPin.id}`);
          const json = await res.json();

          setEditing(false);

          if (json?.data) {
              const updated = json.data as PinDto;
              // ✅ 모달 내부 즉시 반영
              setCurrentPin(updated);
              setContent(updated.content);
              // ✅ 부모 리스트도 갱신
              onChanged?.(updated);
          } else {
              // 혹시 실패하면 내용만 반영
              setCurrentPin({ ...currentPin, content });
              onChanged?.({ ...currentPin, content });
          }

          alert("게시글이 수정되었습니다 ✅");
      } catch (err) {
          console.error("게시글 수정 실패:", err);
          alert("게시글 수정 중 오류가 발생했습니다.");
      }
  };

  // ✅ 삭제
  const deletePin = async () => {
      if (!userId) {
          alert("로그인 후 이용 가능합니다.");
          return;
      } else if (userId != pin.userId) {
          alert("수정 권한이 없습니다.");
          return;
      }
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
            <p className="text-gray-800 leading-relaxed">{currentPin.content}</p>
          )}

          {/* 날짜: 상세 포맷으로 */}
          <div className="text-xs text-gray-500 flex justify-between">
            <span>
              작성: {new Date(currentPin.createdAt).toLocaleString("ko-KR", {
                dateStyle: "medium",
                timeStyle: "short",
              })}
            </span>
            <span>
              수정: {new Date(currentPin.modifiedAt).toLocaleString("ko-KR", {
                dateStyle: "medium",
                timeStyle: "short",
              })}
            </span>
          </div>

          {/* ✅ 태그 섹션 */}
          <div className="mt-3">
            <div className="text-sm font-medium mb-2">🏷️ 태그</div>

            <div className="flex flex-wrap gap-2">
              {(!Array.isArray(tags) || tags.length === 0) && (
                <span className="text-xs text-gray-400">등록된 태그 없음</span>
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
                  className={`px-3 py-1 rounded-md border transition ${
                    isLiked
                      ? "bg-red-100 text-red-600 border-red-300"
                      : "border-gray-300"
                  }`}
                >
                  {isLiked ? "💔 좋아요 취소" : "👍 좋아요"} ({likeCount})
                </button>

                <button
                  onClick={togglePublic}
                  className={`px-3 py-1 rounded-md border transition ${
                    localPublic
                      ? "bg-green-100 text-green-700 border-green-400 hover:bg-green-200"
                      : "bg-gray-100 text-gray-700 border-gray-300 hover:bg-gray-200"
                  }`}
                >
                  {localPublic ? "🔓 공개 중" : "🔒 비공개"}
                </button>

                <button
                  onClick={toggleBookmark}
                  className={`px-3 py-1 rounded-md border transition ${
                    isBookmarked
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