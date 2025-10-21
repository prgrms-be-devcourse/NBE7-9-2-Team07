"use client";
import {createContext, useContext, useEffect, useState} from "react";

type User = { id: number; email: string; name?: string } | null;

type AuthContextType = {
    user: User;
    isLoggedIn: boolean;
    login: (email: string, password: string) => Promise<boolean>;
    logout: () => void;
};

const AuthContext = createContext<AuthContextType>({
    user: null,
    isLoggedIn: false,
    login: async () => false,
    logout: () => {
    },
});

export const AuthProvider = ({children}: { children: React.ReactNode }) => {
    const [user, setUser] = useState<User>(null);
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        if (typeof window !== "undefined") {
            const saved = localStorage.getItem("user");
            if (saved) {
                try {
                    setUser(JSON.parse(saved));
                } catch (error) {
                    console.error("사용자 정보 파싱 실패:", error);
                    localStorage.removeItem("user");
                }
            }
        }
        setIsLoading(false);
    }, []);

    const login = async (email: string, password: string) => {
        try {

            // 1️⃣ 로그인 요청
            const res = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/user/login`, {
                method: "POST",
                headers: {"Content-Type": "application/json"},
                credentials: "include",
                body: JSON.stringify({email, password}),
            });

            if (!res.ok) {
                const errorText = await res.text();
                console.error("❌ 로그인 실패:", res.status, errorText);

                if (res.status === 403) {
                    alert("접근이 거부되었습니다. CORS 설정을 확인해주세요.");
                } else if (res.status === 401) {
                    alert("이메일 또는 비밀번호가 올바르지 않습니다.");
                } else {
                    alert(`로그인 실패: ${res.status}`);
                }
                return false;
            }

            const text = await res.text();

            if (!text || text.trim().length === 0) {
                console.error("❌ 빈 응답");
                alert("서버에서 응답이 없습니다.");
                return false;
            }

            const data = JSON.parse(text);

            // 2️⃣ RsData 구조 확인 및 토큰 저장
            if (data.errorCode === "200") {
                const {apiKey, accessToken, refreshToken} = data.data;

                // 토큰을 localStorage에 저장
                if (typeof window !== "undefined") {
                    localStorage.setItem("apiKey", apiKey);
                    localStorage.setItem("accessToken", accessToken);
                    localStorage.setItem("refreshToken", refreshToken);
                }

                // 3️⃣ 사용자 정보 조회 (/getInfo)
                try {
                    const userRes = await fetch(
                        `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/user/getInfo`,
                        {
                            method: "GET",
                            headers: {
                                "Content-Type": "application/json",
                                "Authorization": `Bearer ${apiKey} ${accessToken}`, // ✅ apiKey + accessToken
                            },
                            credentials: "include",
                        }
                    );

                    if (!userRes.ok) {
                        const errorText = await userRes.text();
                        console.error("❌ 사용자 정보 조회 실패:", userRes.status, errorText);
                        throw new Error(`사용자 정보 조회 실패: ${userRes.status}`);
                    }

                    const userText = await userRes.text();

                    if (!userText || userText.trim().length === 0) {
                        throw new Error("빈 사용자 정보 응답");
                    }

                    const userData = JSON.parse(userText);

                    // 4️⃣ RsData<GetInfoResponse> 구조 파싱
                    if (userData.errorCode === "200") {
                        // ✅ UserDto 필드 매핑
                        const loggedUser = {
                            id: userData.data.id,
                            email: userData.data.email,
                            name: userData.data.userName,
                            createdAt: userData.data.createdAt,
                            modifiedAt: userData.data.modifiedAt,
                        };

                        if (typeof window !== "undefined") {
                            localStorage.setItem("user", JSON.stringify(loggedUser));
                        }

                        setUser(loggedUser);
                        return true;
                    } else {
                        console.error("❌ errorCode가 200이 아님:", userData.errorCode);
                        alert(userData.msg || "사용자 정보를 불러올 수 없습니다.");
                        logout();
                        return false;
                    }

                } catch (userError) {
                    console.error("❌ 사용자 정보 조회 중 에러:", userError);
                    alert("사용자 정보를 불러오는데 실패했습니다.");
                    logout();
                    return false;
                }

            } else {
                console.error("❌ errorCode가 200이 아님:", data.errorCode);
                alert(data.msg || "로그인에 실패했습니다.");
                return false;
            }

        } catch (err) {
            console.error("❌ 로그인 에러:", err);

            if (err instanceof SyntaxError) {
                alert("서버 응답 형식이 올바르지 않습니다.");
            } else if (err instanceof TypeError) {
                alert("네트워크 연결을 확인해주세요.");
            } else {
                alert("로그인 중 오류가 발생했습니다.");
            }
            return false;
        }
    };

    const logout = () => {
        if (typeof window !== "undefined") {
            localStorage.removeItem("user");
            localStorage.removeItem("apiKey");
            localStorage.removeItem("accessToken");
            localStorage.removeItem("refreshToken");
        }
        setUser(null);

        // TODO: 백엔드에 로그아웃 API가 있다면 호출하여 쿠키를 무효화
    };

    const isLoggedIn = !!user;

    if (isLoading) {
        return <div className="flex items-center justify-center min-h-screen">Loading...</div>;
    }

    return (
        <AuthContext.Provider value={{user, isLoggedIn, login, logout}}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);