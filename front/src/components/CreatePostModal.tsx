import { useState } from "react";

type Props = {
  onSubmit: (content: string) => void;
  onClose: () => void;
};

export default function CreatePostModal({ onSubmit, onClose }: Props) {
  const [content, setContent] = useState("");

  return (
    <div className="absolute inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-xl shadow-xl w-[480px] max-w-[90%] relative animate-fadeIn p-6">
        <button className="absolute top-3 right-3 text-gray-500 hover:text-black" onClick={onClose}>
          ✕
        </button>

        <h2 className="text-lg font-semibold mb-3">📝 새 게시글 작성</h2>
        <textarea
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="게시글 내용을 입력하세요..."
          className="w-full border rounded-md p-2 h-32 text-sm resize-none mb-4"
        />
        <button
          onClick={() => onSubmit(content)}
          className="w-full bg-blue-600 text-white py-2 rounded-md hover:bg-blue-700"
        >
          등록하기
        </button>
      </div>
    </div>
  );
}
