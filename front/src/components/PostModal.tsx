import { useEffect, useState } from "react";
import { X } from "lucide-react";
import { PinDto, TagDto } from "../types/types";
import {
  apiAddTagToPin,
  apiDeleteBookmark,
  apiGetLikeUsers,
  apiGetPinTags,
  apiToggleLike,
  apiTogglePublic,
  apiCreateBookmark,
  apiRemoveTagFromPin,
  apiUpdatePin,
  apiDeletePin,
} from "../lib/pincoApi";

type Props = {
  pin: PinDto;
  onClose: () => void;
  userId?: number; // 좋아요/북마크용
  onChanged?: () => void; // 외부에서 목록 갱신 필요시
};

export default function PostModal({ pin, onClose, userId = 1, onChanged }: Props) {
  const [tags, setTags] = useState<TagDto[]>([]);
  const [newTag, setNewTag] = useState("");
  const [likeUsers, setLikeUsers] = useState<{ id: number; userName: string }[]>([]);
  const [editing, setEditing] = useState(false);
  const [content, setContent] = useState(pin.content);

  useEffect(() => {
    (async () => {
      try {
        const t = await apiGetPinTags(pin.id);
        setTags(t);
      } catch {}
      try {
        const u = await apiGetLikeUsers(pin.id);
        setLikeUsers(u);
      } catch {}
    })();
  }, [pin.id]);

  const addTag = async () => {
    if (!newTag.trim()) return;
    await apiAddTagToPin(pin.id, newTag.trim());
    const t = await apiGetPinTags(pin.id);
    setTags(t);
    setNewTag("");
    onChanged?.();
  };

  const removeTag = async (tagId: number) => {
    await apiRemoveTagFromPin(pin.id, tagId);
    const t = await apiGetPinTags(pin.id);
    setTags(t);
    onChanged?.();
  };

  const toggleLike = async () => {
    await apiToggleLike(pin.id, userId);
    const u = await apiGetLikeUsers(pin.id);
    setLikeUsers(u);
    onChanged?.();
  };

  const togglePublic = async () => {
    await apiTogglePublic(pin.id);
    onChanged?.();
  };

  const createBookmark = async () => {
    await apiCreateBookmark(userId, pin.id);
    onChanged?.();
    alert("북마크되었습니다 ✅");
  };

  const deletePin = async () => {
    if (!confirm("이 핀을 삭제할까요?")) return;
    await apiDeletePin(pin.id);
    onChanged?.();
    onClose();
  };

  const saveEdit = async () => {
    await apiUpdatePin(pin.id, content);
    setEditing(false);
    onChanged?.();
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

          {/* 태그 */}
          <div>
            <div className="text-sm font-medium mb-2">태그</div>
            <div className="flex gap-2 flex-wrap">
              {tags.map((t) => (
                <span key={t.id} className="px-2 py-1 text-xs rounded-full bg-gray-100 border border-gray-200">
                  #{t.keyword}
                  <button className="ml-2 text-red-500" onClick={() => removeTag(t.id)}>
                    삭제
                  </button>
                </span>
              ))}
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

          {/* 좋아요/공개/북마크/편집 */}
          <div className="flex flex-wrap gap-2">
            <button onClick={toggleLike} className="px-3 py-1 rounded-md border">
              👍 좋아요 ({likeUsers.length})
            </button>
            <button onClick={togglePublic} className="px-3 py-1 rounded-md border">
              🔁 공개 토글
            </button>
            <button onClick={createBookmark} className="px-3 py-1 rounded-md border">
              ❤️ 북마크
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

          {/* 좋아요한 사람 */}
          <div className="text-sm">
            <span className="font-medium">좋아요한 유저:</span>{" "}
            {likeUsers.length ? likeUsers.map((u) => u.userName).join(", ") : "없음"}
          </div>
        </div>
      </div>
    </div>
  );
}
