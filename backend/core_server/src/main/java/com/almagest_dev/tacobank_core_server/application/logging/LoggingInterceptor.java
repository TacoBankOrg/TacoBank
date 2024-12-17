package com.almagest_dev.tacobank_core_server.application.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // URI를 MDC에 추가
        if (request.getRequestURI().startsWith("/taco/core/transfers")) {
            log.info(request.getRequestURI());
            MDC.put("TRANSFER_REQUEST", "Y");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 요청 처리 완료 후 MDC 초기화
        MDC.clear();
    }
}