package com.almagest_dev.tacobank_core_server.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // 필드가 null이면 직렬화에서 제외
public class CoreResponseDto<T> {
    private String status;
    private String message;
    private T data; // 요청별 응답 데이터

    public CoreResponseDto(String status, String message) {
        this.status = status;
        this.message = message;
    }
}
