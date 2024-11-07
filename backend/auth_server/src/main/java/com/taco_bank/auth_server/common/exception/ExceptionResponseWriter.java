package com.taco_bank.auth_server.common.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taco_bank.auth_server.common.dto.ExceptionResponseDTO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class ExceptionResponseWriter {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Exception 응답 출력
     */
    public static void writeExceptionResponse(HttpServletResponse response, int status, String error, String message) {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        ExceptionResponseDTO exceptionResponse = new ExceptionResponseDTO(error, message);

        // 에러메시지 JSON으로 변환
        String jsonResponse = null;
        try {
            jsonResponse = objectMapper.writeValueAsString(exceptionResponse);
        } catch (IOException e) {
            // JSON 변환 실패
            jsonResponse = "{\"error\": \"" + error + "\", \"message\": \"" + message + "\"}";
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
