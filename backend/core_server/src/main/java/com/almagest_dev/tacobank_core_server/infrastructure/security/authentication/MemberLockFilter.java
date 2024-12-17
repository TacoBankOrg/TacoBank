package com.almagest_dev.tacobank_core_server.infrastructure.security.authentication;

import com.almagest_dev.tacobank_core_server.common.exception.ExceptionResponseWriter;
import com.almagest_dev.tacobank_core_server.common.utils.RedisSessionUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class MemberLockFilter extends OncePerRequestFilter { // 계정 잠김 여부 확인 필터
    private final RedisSessionUtil redisSessionUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("CustomAccountLockFilter::doFilterInternal - authentication : {}", authentication);


        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (principal != null) {
                String username = (String) principal;
                log.info("CustomAccountLockFilter::doFilterInternal - username : {}", username);

                // Redis 에서 확인
                if (redisSessionUtil.isLocked(username)) {
                    ExceptionResponseWriter.writeExceptionResponse(response, HttpServletResponse.SC_FORBIDDEN, "FAILURE", "계정이 잠겼습니다. 잠시 후 다시 시도하거나 고객센터에 문의해주세요.");
                    return; // 계정이 잠겨있으면 요청 중단
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
