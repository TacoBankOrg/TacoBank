package com.almagest_dev.tacobank_core_server.infrastructure.security.authentication;

import com.almagest_dev.tacobank_core_server.common.exception.ExceptionResponseWriter;
import com.almagest_dev.tacobank_core_server.infrastructure.persistence.TokenBlackList;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
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

        // 토큰 유효성 검증
        if (token != null && jwtProvider.validateToken(token)) {
            // 블랙리스트 확인
            if (tokenBlackList.isTokenBlacklisted(token)) {
                log.warn("JwtAuthenticationFilter::doFilterInternal - 블랙리스트 토큰 (token: {})", token);
                ExceptionResponseWriter.writeExceptionResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "FAILURE", "인증 정보가 유효하지 않습니다. 다시 로그인해주세요.");
                return;
            }

            String username = jwtProvider.getUsernameFromToken(token); // 클레임에서 사용자 정보 추출
            Long memberId = jwtProvider.getMemberIdFromToken(token); // 클레임에서 멤버 ID 추출
            log.info("JwtAuthenticationFilter::doFilterInternal - username: {}, memberId: {}", username, memberId);

            // 권한 추출해서 request에 저장
            List<String> roles = jwtProvider.getRolesFromToken(token);
            request.setAttribute("roles", roles);
            Collection<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            log.info("JwtAuthenticationFilter::doFilterInternal - roles from token: " + roles);

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, null, authorities);

            // Member ID를 디테일로 설정
            authentication.setDetails(memberId);
            // Set Authentication
            SecurityContextHolder.getContext().setAuthentication(authentication);
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
