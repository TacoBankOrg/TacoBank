package com.almagest_dev.tacobank_core_server.common.exception;

import com.almagest_dev.tacobank_core_server.common.dto.CoreResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ExceptionResponseWriter {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Exception 응답 출력
     */
    public static void writeExceptionResponse(HttpServletResponse response, int httpStatus, String status, String message) {
        response.setStatus(httpStatus);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        CoreResponseDto exceptionResponse = new CoreResponseDto(status, message);

        // 에러메시지 JSON으로 변환
        String jsonResponse = null;
        try {
            jsonResponse = objectMapper.writeValueAsString(exceptionResponse);
        } catch (IOException e) {
            // JSON 변환 실패
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
