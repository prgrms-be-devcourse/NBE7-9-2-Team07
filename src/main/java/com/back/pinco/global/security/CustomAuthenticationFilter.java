package com.back.pinco.global.security;

import com.back.pinco.domain.user.entity.User;
import com.back.pinco.domain.user.service.UserService;
import com.back.pinco.global.exception.ErrorCode;
import com.back.pinco.global.rq.Rq;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final UserService userService;
    private final JwtTokenProvider tokenProvider;
    private final Rq rq;

    private static final List<String> PERMIT_PATHS = List.of(
            "/api/user/join",
            "/api/user/login",
            "/api/user/reissue"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        final String uri = req.getRequestURI();

        // 공개 경로 or /api/가 아니면 통과
        if (!uri.startsWith("/api/") || PERMIT_PATHS.stream().anyMatch(uri::equals)) {
            chain.doFilter(req, res);
            return;
        }

        // ---- 1) 헤더 우선 파싱: Bearer <apiKey> <accessToken> ----
        String apiKey = "";
        String accessToken = "";

        String authHeader = rq.getHeader("Authorization", "");
        if (!authHeader.isBlank()) {
            if (!authHeader.startsWith("Bearer ")) {
                write401(res, ErrorCode.INVALID_ACCESS_TOKEN); // INVALID_ACCESS_TOKEN
                return;
            }
            // 정확히 3 파트만 허용: Bearer, apiKey, access
            String[] bits = authHeader.split(" ", 3);
            if (bits.length != 3 || bits[1].isBlank() || bits[2].isBlank()) {
                write401(res, ErrorCode.INVALID_ACCESS_TOKEN); // 잘못된 형식도 동일 코드로 처리
                return;
            }
            apiKey = bits[1].trim();
            accessToken = bits[2].trim();
        }

        // 보조 입력: 전용 헤더 & 쿠키
        if (apiKey.isBlank()) apiKey = rq.getHeader("X-API-Key", "");
        if (apiKey.isBlank()) apiKey = rq.getCookieValue("apiKey", "");
        if (accessToken.isBlank()) accessToken = rq.getCookieValue("accessToken", "");

        boolean hasApiKey = !apiKey.isBlank();
        boolean hasAccess = !accessToken.isBlank();

        // ---- 2) 아무 인증 정보도 없으면 401 ----
        if (!hasApiKey && !hasAccess) {
            write401(res, ErrorCode.AUTH_REQUIRED); // AUTH_REQUIRED
            return;
        }

        User user = null;

        // ---- 3) accessToken이 제공된 경우: 유효성 먼저 강제 ----
        if (hasAccess) {
            if (!tokenProvider.isValid(accessToken)) {
                // 자동 재발급 금지: 즉시 401
                write401(res, ErrorCode.INVALID_ACCESS_TOKEN); // INVALID_ACCESS_TOKEN (만료/위조 포함)
                return;
            }
            // 토큰이 유효하면, 토큰 사용자 조회
            Long tokenUserId = tokenProvider.getUserId(accessToken);
            Optional<User> tokenUserOpt = userService.findByIdOptional(tokenUserId);
            if (tokenUserOpt.isEmpty()) {
                write401(res, ErrorCode.INVALID_ACCESS_TOKEN); // 토큰은 유효하나 사용자 실체 없음
                return;
            }
            user = tokenUserOpt.get();

            // apiKey도 함께 온 경우: 같은 사용자여야 함
            if (hasApiKey) {
                // apiKey로도 사용자 확인
                User apiKeyUser;
                try {
                    apiKeyUser = userService.findByApiKey(apiKey);
                } catch (Exception e) {
                    write401(res, ErrorCode.INVALID_API_KEY); // INVALID_API_KEY
                    return;
                }
                if (!apiKeyUser.getId().equals(user.getId())) {
                    // 불일치 = 인증 거부
                    write401(res, ErrorCode.INVALID_ACCESS_TOKEN); // 불일치도 2013으로 통일
                    return;
                }
                // 일치 시 최종 user는 동일
                user = apiKeyUser;
            }
        } else {
            // ---- 4) accessToken이 아예 없고 apiKey만 있는 경우에만 apiKey로 인증 허용 ----
            if (hasApiKey) {
                try {
                    user = userService.findByApiKey(apiKey);
                } catch (Exception e) {
                    write401(res, ErrorCode.INVALID_API_KEY); // INVALID_API_KEY
                    return;
                }
            }
        }

        if (user == null) {
            write401(res, ErrorCode.AUTH_REQUIRED); // 안전망
            return;
        }

        // ---- 5) SecurityContext 주입 ----
        var auth = new UsernamePasswordAuthenticationToken(
                user, null, List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        chain.doFilter(req, res);
    }

    private void write401(HttpServletResponse res, ErrorCode ec) throws IOException {
        if (res.isCommitted()) return;
        res.setStatus(ec.getStatus().value());
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("""
        {"errorCode":"%s","msg":"%s"}
    """.formatted(ec.getCode(), ec.getMessage()));
        res.getWriter().flush();
    }

}




