"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Mail, Lock, User, AtSign, CheckCircle2 } from "lucide-react";

export default function SignUpPage() {
    const router = useRouter(); // ✅ useRouter 훅 선언

    const [form, setForm] = useState({
        username: "",
        password: "",
        nickname: "",
        email: "",
    });
    const [emailVerified, setEmailVerified] = useState(false);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setForm({ ...form, [name]: value });
    };

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();

        if (!emailVerified) {
            alert("이메일 인증을 완료해주세요.");
            return;
        }

        // TODO: 회원가입 API 호출
        alert("회원가입이 완료되었습니다 🎉");

        // ✅ 회원가입 후 로그인 페이지로 이동
        router.push("/user/login");
    };

    const handleEmailVerify = () => {
        // TODO: 이메일 인증 API 호출
        alert("인증 메일이 발송되었습니다 📧");
        setTimeout(() => setEmailVerified(true), 2000);
    };

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 px-4">
            <div className="bg-white shadow-md rounded-xl p-8 w-full max-w-md">
                <h1 className="text-2xl font-bold text-center text-blue-600 mb-6">회원가입</h1>

                <form className="space-y-4" onSubmit={handleSubmit}>
                    {/* 아이디 */}
                    <div className="relative">
                        <User className="absolute left-3 top-3 text-gray-400" size={18} />
                        <input
                            name="username"
                            placeholder="아이디"
                            value={form.username}
                            onChange={handleChange}
                            className="w-full border rounded-md pl-10 pr-3 py-2 focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>

                    {/* 비밀번호 */}
                    <div className="relative">
                        <Lock className="absolute left-3 top-3 text-gray-400" size={18} />
                        <input
                            type="password"
                            name="password"
                            placeholder="비밀번호 (8자 이상)"
                            value={form.password}
                            onChange={handleChange}
                            className="w-full border rounded-md pl-10 pr-3 py-2 focus:ring-2 focus:ring-blue-500"
                            minLength={8}
                            required
                        />
                    </div>

                    {/* 닉네임 */}
                    <div className="relative">
                        <AtSign className="absolute left-3 top-3 text-gray-400" size={18} />
                        <input
                            name="nickname"
                            placeholder="닉네임"
                            value={form.nickname}
                            onChange={handleChange}
                            className="w-full border rounded-md pl-10 pr-3 py-2 focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>

                    {/* 이메일 */}
                    <div className="relative flex gap-2 items-center">
                        <div className="relative flex-1">
                            <Mail className="absolute left-3 top-3 text-gray-400" size={18} />
                            <input
                                type="email"
                                name="email"
                                placeholder="이메일 주소"
                                value={form.email}
                                onChange={handleChange}
                                className="w-full border rounded-md pl-10 pr-3 py-2 focus:ring-2 focus:ring-blue-500"
                                required
                            />
                        </div>
                        <button
                            type="button"
                            onClick={handleEmailVerify}
                            className={`text-sm px-3 py-2 rounded-md ${emailVerified ? "bg-green-500 text-white" : "bg-blue-600 text-white"
                                }`}
                        >
                            {emailVerified ? "인증 완료" : "인증 요청"}
                        </button>
                    </div>

                    {/* 인증 완료 표시 */}
                    {emailVerified && (
                        <div className="flex items-center gap-2 text-green-600 text-sm mt-1">
                            <CheckCircle2 size={16} /> 이메일 인증이 완료되었습니다.
                        </div>
                    )}

                    <button
                        type="submit"
                        className="w-full bg-blue-600 text-white py-2 rounded-md mt-4 hover:bg-blue-700 transition"
                    >
                        회원가입
                    </button>
                </form>
            </div>
        </div>
    );
}
