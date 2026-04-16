package com.mrs.shakes.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 1. 헤더에서 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰이 존재하고 유효한지 검증
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 3. 토큰에서 인증 정보(사용자 객체, 권한)를 가져옴
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            // 4. 세션은 안 쓰지만, 이번 요청이 끝날 때까지 시큐리티가 "인증됨"을 알 수 있게 저장
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        // 5. 다음 필터로 넘김 (중요!)
        filterChain.doFilter(request, response);
    }

    // 헤더에서 "Authorization": "Bearer {TOKEN}" 구조를 찾아 토큰값만 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}