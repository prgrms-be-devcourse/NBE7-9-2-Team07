"use client";

import { useEffect, useRef, useState } from "react";
import { useRouter } from "next/navigation";

export default function MyPage() {
  const router = useRouter();

  // 오른쪽 요약 카드
  const stats = [
    { icon: "📍", label: "등록한 장소", value: 0 },
    { icon: "❤️", label: "받은 좋아요", value: 0 },
    { icon: "👥", label: "팔로워", value: 0 },
    { icon: "🔖", label: "즐겨찾기", value: 0 },
  ];

  // 아래 탭 섹션 표시/상태
  const [showBelow, setShowBelow] = useState(false);
  const [activeTab, setActiveTab] = useState<"record" | "stats">("record");
  const [view, setView] = useState<"grid" | "list">("grid");
  const sentinelRef = useRef<HTMLDivElement | null>(null);

  // 스크롤 센티넬: 보이면 아래 탭 섹션 등장
  useEffect(() => {
    if (!sentinelRef.current) return;
    const io = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting) setShowBelow(true);
      },
      { threshold: 0.2 }
    );
    io.observe(sentinelRef.current);
    return () => io.disconnect();
  }, []);

  return (
    <main className="bg-gray-50 min-h-[160vh]">
      {/* ===== 상단: 3열 레이아웃 ===== */}
      <div className="mx-auto max-w-6xl px-6 py-8 grid grid-cols-1 gap-6 md:grid-cols-[250px_minmax(0,1fr)_220px] items-start">
        {/* ① 왼쪽 프로필 */}
        <aside className="space-y-5 md:col-start-1">
          <div className="bg-white border rounded-2xl p-4 shadow-sm flex items-center gap-3">
            <div className="w-14 h-14 rounded-full bg-gray-200 flex items-center justify-center text-3xl">
              🧑‍🦱
            </div>
            <div>
              <div className="text-base font-semibold">username</div>
              <div className="text-gray-500 text-sm">username@naver.com</div>
            </div>
          </div>

          <button
            className="w-full bg-blue-500 hover:bg-blue-600 text-white text-sm font-medium py-2 rounded-lg transition"
            onClick={() => router.push("/user/mypage/check")}
          >
            회원 정보 수정
          </button>
        </aside>

        {/* ② 가운데 카드들 */}
        <section className="grid grid-cols-1 gap-6 md:col-start-2">
          {/* 레벨 카드 */}
          <div className="bg-blue-50 border border-blue-100 rounded-2xl p-5 shadow-sm">
            <div className="flex justify-between items-center mb-2">
              <span className="font-medium text-blue-700">🏅 레벨 1</span>
              <span className="text-gray-500 text-sm">0개 게시글</span>
            </div>
            <div className="w-full bg-gray-200 h-2 rounded-full">
              <div className="bg-blue-400 h-2 w-1/4 rounded-full" />
            </div>
            <p className="text-sm text-gray-500 mt-2">다음 레벨까지 10개 게시글</p>
          </div>

          {/* 활동온도 카드 */}
          <div className="bg-orange-50 border border-orange-100 rounded-2xl p-5 shadow-sm">
            <div className="flex justify-between items-center mb-2">
              <span className="text-orange-600 font-medium">🔥 활동 온도 36.5℃</span>
              <button className="text-blue-600 text-sm">시작</button>
            </div>
            <div className="w-full bg-gray-200 h-2 rounded-full">
              <div className="bg-black h-2 w-1/3 rounded-full" />
            </div>
            <p className="text-sm text-gray-500 mt-2">
              게시글 작성과 좋아요로 온도를 높여보세요!
            </p>
          </div>
        </section>

        {/* ③ 오른쪽 요약 통계 (sticky 제거) */}
        <div className="flex flex-col gap-4 md:col-start-3 w-[220px] flex-none justify-self-end items-stretch">
          {stats.map((s) => (
            <div
              key={s.label}
              className="bg-white border rounded-2xl shadow-sm py-3 px-3 text-center flex flex-col items-center justify-center w-full"
            >
              <div className="text-2xl mb-1">{s.icon}</div>
              <div className="text-lg font-semibold">{s.value}</div>
              <div className="text-gray-500 text-sm">{s.label}</div>
            </div>
          ))}
        </div>

        {/* 🔻 스크롤 센티넬: 여기 지나면 아래 섹션 등장 */}
        <div ref={sentinelRef} className="md:col-start-2 h-6" />

        {/* ===== 탭 바 ===== */}
        {showBelow && (
          <div className="md:col-start-1 md:col-span-3">
            <div className="w-full bg-gray-100 rounded-full px-2 py-2 shadow-sm">
              <div className="flex w-full">
                <button
                  onClick={() => setActiveTab("record")}
                  className={`flex-1 flex items-center justify-center gap-2 rounded-full py-2 transition font-semibold ${
                    activeTab === "record"
                      ? "bg-white text-gray-900 shadow"
                      : "text-gray-700 hover:text-gray-900"
                  }`}
                >
                  <span>📍</span>
                  <span>내 기록 (0)</span>
                </button>
                <button
                  onClick={() => setActiveTab("stats")}
                  className={`flex-1 flex items-center justify-center gap-2 rounded-full py-2 transition font-semibold ${
                    activeTab === "stats"
                      ? "bg-white text-gray-900 shadow"
                      : "text-gray-700 hover:text-gray-900"
                  }`}
                >
                  <span>📈</span>
                  <span>통계</span>
                </button>
              </div>
            </div>
          </div>
        )}

        {/* ===== 탭 내용: 내 기록 (카드 사이즈 = 통계와 동일화) ===== */}
        {showBelow && activeTab === "record" && (
          <section className="md:col-start-1 md:col-span-3 px-0 py-10">
            <div className="mx-auto max-w-6xl px-6">
              {/* 헤더 + 보기 전환 */}
              <div className="flex items-end justify-between">
                <div>
                  <h2 className="text-2xl font-bold mb-2">내 기록</h2>
                  <p className="text-gray-500">내가 등록한 장소들을 확인하세요</p>
                </div>

                <div className="flex gap-2">
                  <button
                    onClick={() => setView("grid")}
                    className={`w-10 h-10 rounded-lg flex items-center justify-center border ${
                      view === "grid"
                        ? "bg-gray-900 text-white border-gray-900"
                        : "bg-white text-gray-700"
                    }`}
                    title="그리드 보기"
                  >
                    <span className="grid grid-cols-2 gap-0.5">
                      <span className="w-2 h-2 bg-current block" />
                      <span className="w-2 h-2 bg-current block" />
                      <span className="w-2 h-2 bg-current block" />
                      <span className="w-2 h-2 bg-current block" />
                    </span>
                  </button>
                  <button
                    onClick={() => setView("list")}
                    className={`w-10 h-10 rounded-lg flex items-center justify-center border ${
                      view === "list"
                        ? "bg-gray-900 text-white border-gray-900"
                        : "bg-white text-gray-700"
                    }`}
                    title="리스트 보기"
                  >
                    <span className="flex flex-col gap-0.5">
                      <span className="w-4 h-0.5 bg-current block" />
                      <span className="w-4 h-0.5 bg-current block" />
                      <span className="w-4 h-0.5 bg-current block" />
                    </span>
                  </button>
                </div>
              </div>

              {/* 필터 칩 */}
              <div className="flex gap-3 mt-6">
                {["전체 (0)", "공개 (0)", "나만보기 (0)"].map((label) => (
                  <button
                    key={label}
                    className="px-4 py-1.5 bg-white border rounded-full text-sm font-medium hover:bg-gray-50"
                  >
                    {label}
                  </button>
                ))}
              </div>

              {/* === 내 기록 카드 (통계와 동일 크기) === */}
              <div className="mt-6 bg-white border rounded-2xl shadow-sm p-10 md:p-16 flex flex-col justify-between min-h-[520px]">
                <div className="flex flex-col items-center text-center flex-1 justify-center">
                  <div className="text-7xl mb-4">🗓️</div>
                  <h3 className="text-lg font-medium text-gray-800 mb-1">
                    아직 기록이 없습니다
                  </h3>
                  <p className="text-gray-500 mb-8">
                    방문한 장소의 기록을 남겨보세요!
                  </p>
                  <button className="w-full bg-gray-900 text-white rounded-xl py-4 hover:bg-gray-800 transition">
                    첫 기록 작성하기
                  </button>
                </div>
              </div>
            </div>
          </section>
        )}

        {/* ===== 탭 내용: 통계 (카드 크기 = 내 기록과 동일) ===== */}
        {showBelow && activeTab === "stats" && (
          <section className="md:col-start-1 md:col-span-3 px-0 py-10">
            <div className="mx-auto max-w-6xl px-6">
              <div className="bg-white border rounded-2xl shadow-sm p-10 md:p-16 flex flex-col justify-between min-h-[520px]">
                {/* 1️⃣ 활동 현황 */}
                <div>
                  <h3 className="text-base font-semibold mb-5">활동 현황</h3>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div className="rounded-2xl p-6 text-center border bg-blue-50/60">
                      <div className="text-4xl font-bold tracking-tight">0</div>
                      <div className="mt-2 text-gray-600">공개 게시글</div>
                    </div>
                    <div className="rounded-2xl p-6 text-center border bg-violet-50/60">
                      <div className="text-4xl font-bold tracking-tight">0</div>
                      <div className="mt-2 text-gray-600">비공개 기록</div>
                    </div>
                    <div className="rounded-2xl p-6 text-center border bg-amber-50/60">
                      <div className="text-4xl font-bold tracking-tight">0</div>
                      <div className="mt-2 text-gray-600">누적 좋아요</div>
                    </div>
                  </div>
                </div>

                {/* 2️⃣ 인기 게시글 */}
                <div>
                  <h3 className="text-base font-semibold mb-5 mt-10">인기 게시글</h3>
                  <div className="rounded-2xl border bg-gray-50/60 flex flex-col items-center justify-center text-center text-gray-500 p-10 h-[250px]">
                    <div className="text-7xl mb-4">📍</div>
                    <p className="mb-3">아직 등록한 장소가 없습니다</p>
                    <button
                      className="px-4 py-2 rounded-lg border bg-white hover:bg-gray-50"
                      onClick={() => console.log("첫 장소 등록하기 클릭")}
                    >
                      첫 장소 등록하기
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </section>
        )}
      </div>
    </main>
  );
}


