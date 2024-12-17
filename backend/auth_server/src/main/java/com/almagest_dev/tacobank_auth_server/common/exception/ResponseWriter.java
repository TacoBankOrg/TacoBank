package com.almagest_dev.tacobank_auth_server.common.exception;

import com.almagest_dev.tacobank_auth_server.common.dto.AuthResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ResponseWriter {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Exception 응답 출력
     */
    public static <T> void writeExceptionResponse(HttpServletResponse response, int httpStatus, AuthResponseDto<T> authResponseDto) {
        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 에러메시지 JSON으로 변환
        String jsonResponse = null;
        try {
            jsonResponse = objectMapper.writeValueAsString(authResponseDto);
        } catch (IOException e) {
            // JSON 변환 실패
            String status = (authResponseDto.getStatus() != null) ? authResponseDto.getStatus() : "FAILURE";
            String message = (authResponseDto.getMessage() != null) ? authResponseDto.getMessage() : "요청이 실패했습니다.";
            jsonResponse = "{\"status\": \"" + status + "\", \"message\": \"" + message + "\"}";
        }
        // 응답 출력
        try {
            response.getWriter().write(jsonResponse);
        } catch (IOException ex) {
            log.info("ExceptionResponseWriter::writeExceptionResponse - IOException: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
