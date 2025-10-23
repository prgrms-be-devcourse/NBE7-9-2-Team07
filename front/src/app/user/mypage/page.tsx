"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";

type Pin = {
  id: number;
  title: string;
  createdAt: string;
  likes: number;
  isPublic: boolean;
  thumbnail?: string;
};

// 절대 경로
const API_BASE = process.env.NEXT_PUBLIC_API_BASE; // 예: http://localhost:8080

export default function MyPage() {
  const router = useRouter();

  // === 왼쪽 프로필 & 오른쪽 통계 ===
  const [email, setEmail] = useState("");
  const [userName, setUserName] = useState("");
  const [pinCount, setPinCount] = useState(0);
  const [bookmarkCount, setBookmarkCount] = useState(0);
  const [likesCount, setLikesCount] = useState(0);
  const [loading, setLoading] = useState(true); // 통계 로딩
  const [error, setError] = useState<string | null>(null); // 통계 에러

  // === 가운데 핀 목록 ===
  const [pins, setPins] = useState<Pin[]>([]);
  const [pinsLoading, setPinsLoading] = useState(true);
  const [pinsError, setPinsError] = useState<string | null>(null);
  const [view, setView] = useState<"grid" | "list">("grid");
  const [visibility, setVisibility] = useState<"all" | "public" | "private">("all");

  // === 북마크 목록 ===
  const [bookmarks, setBookmarks] = useState<Pin[]>([]);
  const [bookmarksLoading, setBookmarksLoading] = useState(true);
  const [bookmarksError, setBookmarksError] = useState<string | null>(null);
  const [bmView, setBmView] = useState<"grid" | "list">("grid");

  // ✅ 통계/프로필 데이터
  useEffect(() => {
    const fetchMyPage = async () => {
      try {
        const res = await fetch(`${API_BASE}/api/user/mypage`, {
          credentials: "include",
          headers: { Accept: "application/json" },
        });
        const json = await res.json();

        if (!res.ok) throw new Error(json?.msg || "마이페이지 조회 실패");
        const data = json.data;
        console.log("📊 /mypage data:", json.data);

        if (!data) throw new Error("서버 응답에 data 없음");

        setEmail(typeof data.email === "string" ? data.email : "");
        setUserName(typeof data.userName === "string" ? data.userName : "");
        setPinCount(typeof data.pinCount === "number" ? data.pinCount : (typeof data.myPinCount === "number" ? data.myPinCount : 0));
        setBookmarkCount(typeof data.bookmarkCount === "number" ? data.bookmarkCount : 0);
        setLikesCount(typeof data.likesCount === "number" ? data.likesCount : (typeof data.totalLikesCount === "number" ? data.totalLikesCount : 0));
        setError(null);
      } catch (e) {
        setError(e instanceof Error ? e.message : "네트워크 오류");
      } finally {
        setLoading(false);
      }
    };
    fetchMyPage();
  }, []);

  // ✅ 핀 목록(공개/비공개) 가져오기 — any 없음
  useEffect(() => {
    const fetchMyPins = async () => {
      try {
        const res = await fetch(`${API_BASE}/api/user/mypin`, {
          credentials: "include",
          headers: { Accept: "application/json" },
        });
        if (!res.ok) throw new Error("핀 목록을 불러오지 못했습니다.");

        const json: unknown = await res.json();
        if (
          typeof json !== "object" ||
          json === null ||
          !("data" in json) ||
          typeof (json as { data?: unknown }).data !== "object" ||
          (json as { data?: unknown }).data === null
        ) {
          throw new Error("서버 응답 형식이 올바르지 않습니다.");
        }

        const d = (json as { data: Record<string, unknown> }).data;

        const publicRaw  = Array.isArray(d.publicPins)  ? d.publicPins  : [];
        const privateRaw = Array.isArray(d.privatePins) ? d.privatePins : [];

        const toPins = (arr: unknown, defaultPublic: boolean): Pin[] => {
          if (!Array.isArray(arr)) return [];
          const out: Pin[] = [];

          for (const p of arr) {
            if (typeof p !== "object" || p === null) continue;
            const o = p as Record<string, unknown>;

            // id
            let id: number | undefined;
            if (typeof o.id === "number") id = o.id;
            else if (typeof o.pinId === "number") id = o.pinId;
            else if (typeof o.pinId === "string" && !Number.isNaN(Number(o.pinId))) id = Number(o.pinId);
            if (id === undefined) continue;

            // createdAt
            const createdAt =
              (typeof o.createdAt === "string" && o.createdAt) ||
              (typeof o.modifiedAt === "string" && o.modifiedAt) ||
              (typeof o.createdDate === "string" && o.createdDate) ||
              new Date().toISOString();

            // title
            const title =
              (typeof o.title === "string" && o.title) ||
              (typeof o.content === "string" && o.content) ||
              "(제목 없음)";

            // likes
            const likes =
              (typeof o.likes === "number" && o.likes) ||
              (typeof o.likeCount === "number" && o.likeCount) ||
              0;

            // 공개 여부
            const isPublic =
              (typeof o.isPublic === "boolean" && o.isPublic) ||
              (typeof (o as Record<string, unknown>).public === "boolean" &&
                ((o as Record<string, unknown>).public as boolean)) ||
              defaultPublic;

            // 썸네일
            const thumbnail =
              (typeof o.thumbnail === "string" && o.thumbnail) ||
              (typeof o.thumbnailUrl === "string" && o.thumbnailUrl) ||
              (typeof o.imageUrl === "string" && o.imageUrl) ||
              undefined;

            out.push({ id, title, createdAt, likes, isPublic, thumbnail });
          }
          return out;
        };

        const publicPins  = toPins(publicRaw,  true);
        const privatePins = toPins(privateRaw, false);
        const merged = [...publicPins, ...privatePins];

        setPins(merged);
        setPinsError(null);
      } catch (e) {
        setPinsError(e instanceof Error ? e.message : "목록 조회 실패");
        setPins([]);
      } finally {
        setPinsLoading(false);
      }
    };

    fetchMyPins();
  }, []);

  // ✅ 북마크 목록 가져오기 — key: data.bookmarkList
  useEffect(() => {
    const fetchBookmarks = async () => {
      try {
        const res = await fetch(`${API_BASE}/api/user/mybookmark`, {
          credentials: "include",
          headers: { Accept: "application/json" },
        });
        if (!res.ok) throw new Error("북마크 목록을 불러오지 못했습니다.");

        const json: unknown = await res.json();

        if (
          typeof json === "object" &&
          json !== null &&
          "data" in json &&
          typeof (json as { data?: unknown }).data === "object" &&
          (json as { data?: unknown }).data !== null
        ) {
          const dataObj = (json as { data: Record<string, unknown> }).data;

          // ✅ 서버가 bookmarkList로 내려줌(없으면 대체 키들 시도)
          const listUnknown =
            (Array.isArray(dataObj["bookmarkList"]) && dataObj["bookmarkList"]) ||
            (Array.isArray(dataObj["bookmarks"]) && dataObj["bookmarks"]) ||
            (Array.isArray(dataObj["pins"]) && dataObj["pins"]) ||
            [];

          const parsed: Pin[] = [];
          for (const item of listUnknown) {
            if (typeof item !== "object" || item === null) continue;

            // { pin: {...} } 형태도 고려
            const obj = item as Record<string, unknown>;
            const src =
              typeof obj["pin"] === "object" && obj["pin"] !== null
                ? (obj["pin"] as Record<string, unknown>)
                : obj;

            // id
            let id: number | undefined;
            if (typeof src["id"] === "number") id = src["id"];
            else if (typeof src["pinId"] === "number") id = src["pinId"];
            else if (typeof src["pinId"] === "string" && !Number.isNaN(Number(src["pinId"])))
              id = Number(src["pinId"]);
            if (id === undefined) continue;

            // createdAt
            const createdAt =
              (typeof src["createdAt"] === "string" && src["createdAt"]) ||
              (typeof src["modifiedAt"] === "string" && src["modifiedAt"]) ||
              (typeof src["createdDate"] === "string" && src["createdDate"]) ||
              new Date().toISOString();

            // title
            const title =
              (typeof src["title"] === "string" && src["title"]) ||
              (typeof src["subject"] === "string" && src["subject"]) ||
              (typeof src["content"] === "string" && src["content"]) ||
              "(제목 없음)";

            // likes
            const likes =
              (typeof src["likes"] === "number" && src["likes"]) ||
              (typeof src["likeCount"] === "number" && src["likeCount"]) ||
              0;

            // 공개 여부
            const isPublic =
              (typeof src["isPublic"] === "boolean" && src["isPublic"]) ||
              (typeof src["public"] === "boolean" && (src["public"] as boolean)) ||
              true;

            // 썸네일
            const thumbnail =
              (typeof src["thumbnail"] === "string" && src["thumbnail"]) ||
              (typeof src["thumbnailUrl"] === "string" && src["thumbnailUrl"]) ||
              (typeof src["imageUrl"] === "string" && src["imageUrl"]) ||
              undefined;

            parsed.push({ id, title, createdAt, likes, isPublic, thumbnail });
          }

          setBookmarks(parsed);
          setBookmarksError(null);
        } else {
          setBookmarksError("서버 응답 형식이 올바르지 않습니다.");
        }
      } catch (err) {
        setBookmarksError(err instanceof Error ? err.message : "알 수 없는 오류");
      } finally {
        setBookmarksLoading(false); // ✅ 로딩 종료는 북마크 전용 상태로
      }
    };

    fetchBookmarks();
  }, []);

  const stats = [
    { icon: "📍", label: "등록한 핀", value: pinCount },
    { icon: "❤️", label: "받은 좋아요", value: likesCount },
    { icon: "🔖", label: "북마크", value: bookmarkCount },
  ];

  const filteredPins =
    visibility === "all"
      ? pins
      : visibility === "public"
      ? pins.filter((p) => p.isPublic)
      : pins.filter((p) => !p.isPublic);

  return (
    <main className="bg-gray-50 min-h-[100vh]">
      <div className="mx-auto max-w-6xl px-6 py-8 grid grid-cols-1 gap-6 md:grid-cols-[250px_minmax(0,1fr)_220px] items-start">
        {/* 왼쪽 프로필 */}
        <aside className="space-y-5 md:col-start-1">
          <div className="bg-white border rounded-2xl p-4 shadow-sm flex items-center gap-3">
            <div className="w-14 h-14 rounded-full bg-gray-200 flex items-center justify-center text-3xl">🧑‍🦱</div>
            <div>
              <div className="text-base font-semibold">{loading ? "로딩 중..." : userName || "-"}</div>
              <div className="text-gray-500 text-sm">{loading ? "" : email}</div>
            </div>
          </div>
          <button
            className="w-full bg-blue-500 hover:bg-blue-600 text-white text-sm font-medium py-2 rounded-lg transition"
            onClick={() => router.push("/user/mypage/check")}
          >
            회원 정보 수정
          </button>
        </aside>

        {/* 가운데 핀 목록 */}
        <section className="grid grid-cols-1 gap-6 md:col-start-2">
          {/* 내가 작성한 핀 */}
          <div className="bg-orange-50 border border-orange-100 rounded-2xl p-5 shadow-sm">
            <div className="flex justify-between items-center mb-3">
              <div className="flex items-baseline gap-2">
                <h3 className="text-orange-700 font-semibold">📍 내가 작성한 핀</h3>
                <span className="text-gray-500 text-sm">
                  {pinsLoading ? "…" : `${filteredPins.length}개`}
                </span>
              </div>

              <div className="flex gap-2">
                {(["all", "public", "private"] as const).map((v) => (
                  <button
                    key={v}
                    onClick={() => setVisibility(v)}
                    className={`px-3 py-1 rounded-full text-sm border transition ${
                      visibility === v
                        ? "bg-gray-900 text-white border-gray-900"
                        : "bg-white text-gray-700 border-gray-200 hover:bg-gray-50"
                    }`}
                  >
                    {v === "all" ? "전체" : v === "public" ? "공개" : "비공개"}
                  </button>
                ))}
                {(["grid", "list"] as const).map((v) => (
                  <button
                    key={v}
                    onClick={() => setView(v)}
                    className={`px-3 py-1 rounded-full text-sm border transition ${
                      view === v
                        ? "bg-gray-900 text-white border-gray-900"
                        : "bg-white text-gray-700 border-gray-200 hover:bg-gray-50"
                    }`}
                  >
                    {v === "grid" ? "그리드" : "리스트"}
                  </button>
                ))}
              </div>
            </div>

            <div className="bg-white border border-gray-100 rounded-xl p-4">
              {pinsLoading ? (
                <div className="text-sm text-gray-500 py-8 text-center">불러오는 중…</div>
              ) : pinsError ? (
                <div className="text-sm text-red-500 py-8 text-center">{pinsError}</div>
              ) : filteredPins.length === 0 ? (
                <div className="text-sm text-gray-400 py-8 text-center">게시물이 없습니다.</div>
              ) : view === "grid" ? (
                <ul className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                  {filteredPins.map((p) => (
                    <li
                      key={p.id}
                      className="group border border-gray-100 rounded-xl overflow-hidden hover:shadow-md transition cursor-pointer bg-white"
                      onClick={() => router.push(`/pin/${p.id}`)}
                    >
                      <div className="aspect-video bg-gray-100 relative overflow-hidden">
                        {p.thumbnail ? (
                          // eslint-disable-next-line @next/next/no-img-element
                          <img src={p.thumbnail} alt="thumbnail" className="w-full h-full object-cover" />
                        ) : (
                          <div className="w-full h-full flex items-center justify-center text-3xl">📍</div>
                        )}
                        <div className="absolute top-2 left-2 text-xs px-2 py-0.5 rounded-full bg-white/90 border">
                          {p.isPublic ? "공개" : "비공개"}
                        </div>
                      </div>
                      <div className="p-3">
                        <h4 className="font-medium text-gray-900 line-clamp-1 group-hover:underline">{p.title}</h4>
                        <div className="mt-1 flex items-center justify-between text-xs text-gray-500">
                          <span>{new Date(p.createdAt).toLocaleDateString("ko-KR")}</span>
                          <span>❤️ {p.likes}</span>
                        </div>
                      </div>
                    </li>
                  ))}
                </ul>
              ) : (
                <ul className="divide-y">
                  {filteredPins.map((p) => (
                    <li
                      key={p.id}
                      className="py-3 flex items-center justify-between gap-3 hover:bg-gray-50 px-2 rounded-lg transition cursor-pointer"
                      onClick={() => router.push(`/pin/${p.id}`)}
                    >
                      <div className="flex items-center gap-3 min-w-0">
                        <div className="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center overflow-hidden flex-none">
                          {p.thumbnail ? (
                            // eslint-disable-next-line @next/next/no-img-element
                            <img src={p.thumbnail} alt="thumbnail" className="w-full h-full object-cover" />
                          ) : (
                            <span className="text-xl">📍</span>
                          )}
                        </div>
                        <div className="min-w-0">
                          <div className="flex items-center gap-2">
                            <span className="text-xs px-2 py-0.5 rounded-full bg-gray-100 border text-gray-700">
                              {p.isPublic ? "공개" : "비공개"}
                            </span>
                            <span className="text-xs text-gray-500">
                              {new Date(p.createdAt).toLocaleDateString("ko-KR")}
                            </span>
                          </div>
                          <h4 className="text-sm font-medium text-gray-900 truncate">{p.title}</h4>
                        </div>
                      </div>
                      <div className="text-xs text-gray-600 flex items-center gap-1 flex-none">❤️ {p.likes}</div>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>

          {/* 내가 북마크한 핀 */}
          <div className="bg-blue-50 border border-blue-100 rounded-2xl p-5 shadow-sm">
            <div className="flex justify-between items-center mb-3">
              <div className="flex items-baseline gap-2">
                <h3 className="text-blue-700 font-semibold">🔖 내가 북마크한 핀</h3>
                <span className="text-gray-500 text-sm">
                  {bookmarksLoading ? "…" : `${bookmarks.length}개`}
                </span>
              </div>
              <div className="flex gap-2">
                {(["grid", "list"] as const).map((v) => (
                  <button
                    key={v}
                    onClick={() => setBmView(v)}
                    className={`px-3 py-1 rounded-full text-sm border transition ${
                      bmView === v
                        ? "bg-gray-900 text-white border-gray-900"
                        : "bg-white text-gray-700 border-gray-200 hover:bg-gray-50"
                    }`}
                  >
                    {v === "grid" ? "그리드" : "리스트"}
                  </button>
                ))}
              </div>
            </div>

            <div className="bg-white border border-gray-100 rounded-xl p-4">
              {bookmarksLoading ? (
                <div className="text-sm text-gray-500 py-8 text-center">불러오는 중…</div>
              ) : bookmarksError ? (
                <div className="text-sm text-red-500 py-8 text-center">{bookmarksError}</div>
              ) : bookmarks.length === 0 ? (
                <div className="text-sm text-gray-400 py-8 text-center">북마크한 게시물이 없습니다.</div>
              ) : bmView === "grid" ? (
                <ul className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                  {bookmarks.map((b) => (
                    <li
                      key={b.id}
                      className="group border border-gray-100 rounded-xl overflow-hidden hover:shadow-md transition cursor-pointer bg-white"
                      onClick={() => router.push(`/pin/${b.id}`)}
                    >
                      <div className="aspect-video bg-gray-100 relative overflow-hidden">
                        {b.thumbnail ? (
                          // eslint-disable-next-line @next/next/no-img-element
                          <img src={b.thumbnail} alt="thumbnail" className="w-full h-full object-cover" />
                        ) : (
                          <div className="w-full h-full flex items-center justify-center text-3xl">📍</div>
                        )}
                        <div className="absolute top-2 left-2 text-xs px-2 py-0.5 rounded-full bg-white/90 border">
                          {b.isPublic ? "공개" : "비공개"}
                        </div>
                      </div>
                      <div className="p-3">
                        <h4 className="font-medium text-gray-900 line-clamp-1 group-hover:underline">{b.title}</h4>
                        <div className="mt-1 flex items-center justify-between text-xs text-gray-500">
                          <span>{new Date(b.createdAt).toLocaleDateString("ko-KR")}</span>
                          <span>❤️ {b.likes}</span>
                        </div>
                      </div>
                    </li>
                  ))}
                </ul>
              ) : (
                <ul className="divide-y">
                  {bookmarks.map((b) => (
                    <li
                      key={b.id}
                      className="py-3 flex items-center justify-between gap-3 hover:bg-gray-50 px-2 rounded-lg transition cursor-pointer"
                      onClick={() => router.push(`/pin/${b.id}`)}
                    >
                      <div className="flex items-center gap-3 min-w-0">
                        <div className="w-12 h-12 bg-gray-100 rounded-lg flex items-center justify-center overflow-hidden flex-none">
                          {b.thumbnail ? (
                            // eslint-disable-next-line @next/next/no-img-element
                            <img src={b.thumbnail} alt="thumbnail" className="w-full h-full object-cover" />
                          ) : (
                            <span className="text-xl">📍</span>
                          )}
                        </div>
                        <div className="min-w-0">
                          <div className="flex items-center gap-2">
                            <span className="text-xs px-2 py-0.5 rounded-full bg-gray-100 border text-gray-700">
                              {b.isPublic ? "공개" : "비공개"}
                            </span>
                            <span className="text-xs text-gray-500">
                              {new Date(b.createdAt).toLocaleDateString("ko-KR")}
                            </span>
                          </div>
                          <h4 className="text-sm font-medium text-gray-900 truncate">{b.title}</h4>
                        </div>
                      </div>
                      <div className="text-xs text-gray-600 flex items-center gap-1 flex-none">❤️ {b.likes}</div>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        </section>

        {/* 오른쪽 통계 */}
        <div className="flex flex-col gap-4 md:col-start-3 w-[220px] flex-none justify-self-end items-stretch">
          {loading ? (
            <div className="bg-white border rounded-2xl shadow-sm py-6 px-3 text-center text-sm text-gray-500">불러오는 중…</div>
          ) : error ? (
            <div className="bg-white border rounded-2xl shadow-sm py-6 px-3 text-center text-sm text-red-500">{error}</div>
          ) : (
            stats.map((s) => (
              <div
                key={s.label}
                className="bg-white border rounded-2xl shadow-sm py-3 px-3 text-center flex flex-col items-center justify-center w-full"
              >
                <div className="text-2xl mb-1">{s.icon}</div>
                <div className="text-lg font-semibold">{s.value.toLocaleString()}</div>
                <div className="text-gray-500 text-sm">{s.label}</div>
              </div>
            ))
          )}
        </div>
      </div>
    </main>
  );
}





