package com.almagest_dev.tacobank_auth_server.auth.infrastructure.security.authentication;

import com.almagest_dev.tacobank_auth_server.auth.infrastructure.persistence.TokenBlackList;
import com.almagest_dev.tacobank_auth_server.common.dto.AuthResponseDto;
import com.almagest_dev.tacobank_auth_server.common.exception.ResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.http.Cookie;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final TokenBlackList tokenBlackList;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("JwtAuthenticationFilter::doFilterInternal");
        // 토큰 추출
        String token = getTokenFromCookies(request.getCookies());
        log.info("JwtAuthenticationFilter::doFilterInternal - token: " + token);
        if (token != null && jwtProvider.validateToken(token)) {
            // 블랙리스트 확인
            if (tokenBlackList.isTokenBlacklisted(token)) {
                log.warn("JwtAuthenticationFilter::doFilterInternal - 블랙리스트 토큰 (token: {})", token);
                ResponseWriter.writeExceptionResponse(response, HttpServletResponse.SC_UNAUTHORIZED, new AuthResponseDto<>("FAILURE", "인증 정보가 유효하지 않습니다. 다시 로그인해주세요."));
                return;
            }

            Authentication authentication = jwtProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("JwtAuthenticationFilter::doFilterInternal - getAuthentication : " + SecurityContextHolder.getContext().getAuthentication());
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Cookie 에서 토큰 추출
     */
    private String getTokenFromCookies(Cookie[] cookies) {
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if ("Authorization".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
