"use client";

import { useEffect, useMemo, useState } from "react";

type Post = { id: string; title: string; content: string };

// 데모 데이터
const MOCK: Record<string, Post[]> = {
  "2025-10-14": [
    { id: "1", title: "한강 산책", content: "바람 시원~ 기록 남김" },
    { id: "2", title: "신상 카페", content: "라떼 맛집 발견 ☕️" },
  ],
};

const pad = (n: number) => (n < 10 ? `0${n}` : `${n}`);
const toKey = (y: number, m0: number, d: number) => `${y}-${pad(m0 + 1)}-${pad(d)}`;

// 데모용 fetch
async function fetchPostsByDate(key: string): Promise<Post[]> {
  await new Promise((r) => setTimeout(r, 150));
  return MOCK[key] ?? [];
}

export default function CalendarPage() {
  const today = new Date();

  const [year, setYear] = useState(today.getFullYear());
  const [month, setMonth] = useState(today.getMonth()); // 0=Jan
  const [selectedDay, setSelectedDay] = useState<number | null>(today.getDate());
  const [selectedYear, setSelectedYear] = useState<number | null>(today.getFullYear());
  const [selectedMonth, setSelectedMonth] = useState<number | null>(today.getMonth());

  const firstDay = new Date(year, month, 1);
  const lastDay = new Date(year, month + 1, 0);
  const startOffset = firstDay.getDay(); // 0~6 (일~토)
  const daysInMonth = lastDay.getDate();


  // 달력 셀(앞쪽 빈칸 + 날짜)
  const cells: (number | null)[] = [
    ...Array.from({ length: startOffset }, () => null),
    ...Array.from({ length: daysInMonth }, (_, i) => i + 1),
  ];

  const monthNames = [
    "1월","2월","3월","4월","5월","6월",
    "7월","8월","9월","10월","11월","12월"
  ];
  const weekDays = ["일","월","화","수","목","금","토"];

  const isToday = (y: number, m0: number, d: number) =>
    today.getFullYear() === y && today.getMonth() === m0 && today.getDate() === d;

  // 이전/다음달
  const prevMonth = () => {
    if (month === 0) {
      setYear((y) => y - 1);
      setMonth(11);
    } else {
      setMonth((m) => m - 1);
    }
  };
  const nextMonth = () => {
    if (month === 11) {
      setYear((y) => y + 1);
      setMonth(0);
    } else {
      setMonth((m) => m + 1);
    }
  };

  // 선택된 날짜 키 및 게시물 로딩
  const selectedKey = useMemo(() => {
    if (!selectedDay || selectedYear == null || selectedMonth == null) return null;
    return toKey(selectedYear, selectedMonth, selectedDay);
  }, [selectedDay, selectedYear, selectedMonth]);

  const [posts, setPosts] = useState<Post[] | null>([]);
  useEffect(() => {
    let alive = true;
    if (!selectedKey) { setPosts([]); return; }
    setPosts(null); // 로딩 표시
    fetchPostsByDate(selectedKey).then((data) => {
      if (alive) setPosts(data);
    });
    return () => { alive = false; };
  }, [selectedKey]);



  return (
    <div className="[color-scheme:light] min-h-screen bg-gray-50 p-6">
      {/* 2열 레이아웃 */}
      <div className="mx-auto max-w-6xl grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* 왼쪽: 달력 카드 */}
        <section className="bg-white rounded-2xl shadow-sm p-6">
          <h1 className="text-2xl font-semibold mb-4">
            {year}년 {monthNames[month]}
          </h1>

          {/* 상단 통계 */}
          <div className="flex gap-5 mb-6">
            {[
              { label: "이달의 방문", value: 0, color: "bg-blue-50" },
              { label: "방문한 날", value: 0, color: "bg-green-50" },
              { label: "받은 좋아요", value: 0, color: "bg-purple-50" },
            ].map((it, i) => (
              <div
                key={i}
                className={`w-50 h-20 rounded-xl shadow-sm flex flex-col items-center justify-center ${it.color}`}
              >
                <div className="text-2xl font-bold">{it.value}</div>
                <div className="text-sm text-gray-500">{it.label}</div>
              </div>
            ))}
          </div>

          {/* 달력 헤더 */}
          <div className="flex items-center justify-between w-[515px] mb-2">
            <button onClick={prevMonth} className="px-2 py-1 rounded hover:bg-gray-100">&lt;</button>
            <div className="text-lg font-medium">{monthNames[month]} {year}</div>
            <button onClick={nextMonth} className="px-2 py-1 rounded hover:bg-gray-100">&gt;</button>
          </div>

          {/* 요일 */}
          <div className="grid grid-cols-7 gap-1 text-center w-[515px] mb-1 text-gray-500 text-sm">
            {weekDays.map((d) => <div key={d}>{d}</div>)}
          </div>

          {/* 날짜 */}
          <div className="grid grid-cols-7 gap-1 w-[515px]">
            {cells.map((d, idx) => {
              if (d === null) {
                return <div key={idx} className="h-10 pointer-events-none invisible" aria-hidden />;
              }

              const isSelectedHere =
                selectedDay != null &&
                selectedYear === year &&
                selectedMonth === month &&
                selectedDay === d;

              return (
                <button
                  key={idx}
                  type="button"
                  onClick={() => {
                    setSelectedDay(d);
                    setSelectedYear(year);
                    setSelectedMonth(month);
                  }}
                  className={[
                    "h-10 rounded flex items-center justify-center transition",
                    "hover:bg-blue-50",
                    isSelectedHere ? "bg-blue-100 border border-blue-400" : "",
                    isToday(year, month, d) ? "font-semibold" : "",
                  ].join(" ")}
                >
                  {d}
                </button>
              );
            })}
          </div>

        </section>

        {/* 오른쪽: 게시물 패널 */}
        <section className="bg-white rounded-2xl shadow-sm p-6 min-h-64">
          <header className="flex items-center justify-between mb-4">
            <div className="text-xl font-semibold">
              {selectedDay ? `${month + 1}월 ${selectedDay}일` : `${month + 1}월`}
              {selectedDay && isToday(year, month, selectedDay) && (
                <span className="ml-2 text-xs px-2 py-1 rounded-full bg-gray-100">오늘</span>
              )}
            </div>
            <div className="text-xs px-2 py-1 rounded-full bg-gray-100">
              {posts?.length ?? 0}개
            </div>
          </header>

          {posts === null ? (
            <div className="flex items-center justify-center text-gray-400 mt-50">로딩 중…</div>
          ) : posts.length === 0 ? (
            <div className="flex flex-col items-center justify-center text-gray-400 mt-40">
              <div className="text-5xl mb-3">📍</div>
              <div className="font-medium mb-1">이 날의 방문 기록이 없습니다</div>
              <div className="text-sm">새로운 장소를 방문하고 기록해보세요!</div>
            </div>
          ) : (
            <ul className="space-y-3">
              {posts.map((p) => (
                <li key={p.id} className="border rounded-xl p-4 hover:bg-gray-50">
                  <div className="font-semibold mb-1">{p.title}</div>
                  <div className="text-sm text-gray-600">{p.content}</div>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  );
}


