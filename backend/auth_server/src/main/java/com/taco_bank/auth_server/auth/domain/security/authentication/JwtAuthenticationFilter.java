package com.taco_bank.auth_server.auth.domain.security.authentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.info("JwtAuthenticationFilter::doFilterInternal");
        // 토큰 추출
        String token = resolveToken(request);
        log.info("JwtAuthenticationFilter::doFilterInternal - token: " + token);
        if (token != null && jwtProvider.validateToken(token)) {
            Authentication authentication = jwtProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("JwtAuthenticationFilter::doFilterInternal - setContext");
        }
        log.info("JwtAuthenticationFilter::doFilterInternal - " + SecurityContextHolder.getContext().getAuthentication().getName() + " | " + SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        filterChain.doFilter(request, response);
    }

    /**
     * Header 에서 토큰 추출 ('Bearer' 방식)
     */
    private String resolveToken(HttpServletRequest request) {
        log.info("JwtAuthenticationFilter::resolveToken");
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
