// src/lib/auth.ts
import jwt from "jsonwebtoken";

const SECRET = "mock-secret-key"; // ⚠️ 실제 백엔드에서는 환경변수로 관리

export function mockLogin(email: string, password: string) {
  // ✅ 간단한 유효성 검사
  if (email === "test@test.com" && password === "1234") {
    const token = jwt.sign({ email, role: "USER" }, SECRET, { expiresIn: "1h" });
    return token;
  }
  throw new Error("Invalid credentials");
}

export function verifyToken(token: string) {
  try {
    return jwt.verify(token, SECRET);
  } catch {
    return null;
  }
}
