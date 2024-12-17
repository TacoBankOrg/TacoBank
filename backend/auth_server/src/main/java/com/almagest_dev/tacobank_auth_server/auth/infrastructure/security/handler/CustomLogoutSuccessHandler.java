package com.almagest_dev.tacobank_auth_server.auth.infrastructure.security.handler;


import com.almagest_dev.tacobank_auth_server.auth.infrastructure.persistence.TokenBlackList;
import com.almagest_dev.tacobank_auth_server.auth.infrastructure.security.authentication.JwtProvider;
import com.almagest_dev.tacobank_auth_server.common.dto.AuthResponseDto;
import com.almagest_dev.tacobank_auth_server.common.exception.ResponseWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutSuccessHandler implements LogoutSuccessHandler {
    private final JwtProvider jwtProvider;
    private final TokenBlackList tokenBlackList;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("CustomLogoutSuccessHandler::onLogoutSuccess START");

        // 토큰 추출
        String token = getTokenFromCookies(request.getCookies());
        log.info("CustomLogoutSuccessHandler::onLogoutSuccess - token: " + token);

        // 토큰 유효성 검증
        if (token != null && jwtProvider.validateToken(token)) {
            // 토큰 남은 만료 시간 계산
            long remainingTime = jwtProvider.getRemainingExpiration(token);

            // 블랙리스트에 추가
            tokenBlackList.addTokenToBlackList(token, remainingTime);
            log.info("CustomLogoutSuccessHandler - 토큰이 블랙리스트에 추가되었습니다: {}", token);

            if (authentication == null) {
                ResponseWriter.writeExceptionResponse(response, HttpServletResponse.SC_OK, new AuthResponseDto<>("FAILURE", "이미 로그아웃된 상태입니다."));
                log.info("CustomLogoutSuccessHandler::onLogoutSuccess 이미 로그아웃 상태");
            } else {
                ResponseWriter.writeExceptionResponse(response, HttpServletResponse.SC_OK, new AuthResponseDto<>("SUCCESS", "로그아웃 되었습니다."));
                log.info("CustomLogoutSuccessHandler::onLogoutSuccess 로그아웃 성공");
            }
        }

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