package com.suppleit.backend.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;


import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final JwtTokenBlacklistService tokenBlacklistService; // 추가

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        try {
            // 공지사항 조회 API는 인증 절차를 건너뛰도록 설정
            String requestUri = request.getRequestURI();
            // ✅ 인증 없이 허용할 GET API 목록
            if ((requestUri.startsWith("/api/notice") || requestUri.startsWith("/api/reviews")) 
            && request.getMethod().equals("GET")) {
            chain.doFilter(request, response);
            return;
            }
            String token = resolveToken(request);

            // ✅ 토큰이 있을 때만 처리 (없으면 그냥 통과)
            if (token != null) {
                if (tokenBlacklistService.isBlacklisted(token)) {
                    log.info("Token is blacklisted (logged out): {}", token.substring(0, 10) + "...");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been invalidated (logged out)");
                    return;
                }

                if (!jwtTokenProvider.validateToken(token)) {
                    log.warn("Invalid or expired token");
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
                    return;
                }

                String email = jwtTokenProvider.getEmail(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (userDetails != null) {
                    Authentication auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                    );
                    ((UsernamePasswordAuthenticationToken) auth)
                            .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                } else {
                    log.warn("No user details found for email: {}", email);
                }
            }

            chain.doFilter(request, response);

        } catch (Exception e) {
            log.error("JWT Filter Error", e);
            // 수정: 예외가 발생해도 인증 없이 허용된 요청은 그대로 통과
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication error: " + e.getMessage());
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            // 토큰이 없으면 null 반환 (예외 X)
            return null;
        }
        return bearerToken.substring(7);
    }
}