package com.almagest_dev.tacobank_core_server.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JsonUtil {
    private final ObjectMapper objectMapper;

    /**
     * 객체를 JSON 문자열로 변환
     * @param object 변환할 객체
     * @return JSON 문자열
     * @throws RuntimeException 변환 실패 시 예외 발생
     */
    public String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 직렬화 중 오류 발생", e);
        }
    }

    /**
     * JSON 문자열을 객체로 변환
     *
     * @param json  변환할 JSON 문자열
     * @param clazz 변환 대상 클래스 타입
     * @param <T>   변환 대상 타입
     * @return 변환된 객체
     * @throws RuntimeException 변환 실패 시 예외 발생
     */
    public <T> T fromJsonString(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 역직렬화 중 오류 발생", e);
        }
    }
}
