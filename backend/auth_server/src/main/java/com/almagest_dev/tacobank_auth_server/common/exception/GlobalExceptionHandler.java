package com.almagest_dev.tacobank_auth_server.common.exception;

import com.almagest_dev.tacobank_auth_server.common.dto.AuthResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400 Bad Request
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("handleIllegalArgumentException - " + ex.getMessage());
        AuthResponseDto response = new AuthResponseDto("FAILURE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.warn("MethodArgumentNotValidException - " + ex.getMessage());
        List<String> errorMessages = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.toList());

        if (errorMessages.size() == 1) {
            // 오류 메시지가 하나면 String 형태로 반환
            AuthResponseDto response = new AuthResponseDto("FAILURE", errorMessages.get(0));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            // 여러 개의 오류 메시지가 있으면 Map 형태로 반환
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Validation Error");
            response.put("message", errorMessages);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        log.warn("HttpMessageNotReadableException - " + ex.getMessage());
        AuthResponseDto response = new AuthResponseDto("FAILURE", "요청 본문이 비어있거나 올바르지 않습니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    @ExceptionHandler(DataIntegrityViolationException.class) // 데이터 무결성 제약 조건을 위반했을 때 발생하는 예외 (데이터베이스에 저장할 때 NULL 값이 들어가면 안 되는 칼럼에 NULL이 들어간 경우나 고유 제약 조건을 위반한 경우)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("DataIntegrityViolationException - " + ex.getMessage());
        AuthResponseDto response = new AuthResponseDto("FAILURE", "필수 데이터가 누락되었거나 잘못된 데이터가 입력되었습니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("NoResourceFoundException - " + ex.getMessage());
        AuthResponseDto response = new AuthResponseDto("FAILURE", "잘못된 요청입니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        log.warn("HttpRequestMethodNotSupportedException - " + ex.getMessage());
        AuthResponseDto response = new AuthResponseDto("FAILURE", "지원하지 않는 요청입니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(InvalidTokenException.class) // 데이터 무결성 제약 조건을 위반했을 때 발생하는 예외 (데이터베이스에 저장할 때 NULL 값이 들어가면 안 되는 칼럼에 NULL이 들어간 경우나 고유 제약 조건을 위반한 경우)
    public ResponseEntity<?> handleInvalidTokenException(InvalidTokenException ex) {
        log.warn("InvalidTokenException - " + ex.getMessage());
        AuthResponseDto response = new AuthResponseDto("FAILURE", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    // 포괄적인 서버 오류 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleServerError(Exception ex) {
        log.warn("Exception - " + ex.getMessage());
        AuthResponseDto response = new AuthResponseDto("FAILURE", "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        ex.printStackTrace();  // 서버 로그에 전체 오류 메시지 출력 (디버깅용)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<?> handleIllegalStateException(IllegalStateException ex) {
        log.warn("IllegalStateException - " + ex.getMessage());
        AuthResponseDto response = new AuthResponseDto("FAILURE", "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
