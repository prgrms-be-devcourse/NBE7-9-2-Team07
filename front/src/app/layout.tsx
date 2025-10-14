// src/app/layout.tsx
import "./globals.css";
import Header from "@/components/layout/Header";

export const metadata = {
  title: "PinCo",
  description: "위치 기반 게시물 플랫폼",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ko">
      <body className="bg-gray-50 text-gray-900">
        {/* 공통 Header */}
        <Header />
        <main>{children}</main>
      </body>
    </html>
  );
}
