package com.almagest_dev.tacobank_auth_server.auth.infrastructure.security.handler;

import com.almagest_dev.tacobank_auth_server.common.dto.AuthResponseDto;
import com.almagest_dev.tacobank_auth_server.common.exception.ResponseWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        log.info("CustomAccessDeniedHandler - Exception Message: " + accessDeniedException.getMessage());
        ResponseWriter.writeExceptionResponse(response, HttpServletResponse.SC_FORBIDDEN, new AuthResponseDto<>("FAILURE", "접근 권한이 없습니다."));
    }
}
