package com.almagest_dev.tacobank_core_server.infrastructure.external.naver.dto.sms;

import lombok.Data;

@Data
public class NaverSmsResponseDto {
    private String requestId;   // 요청 아이디
    private String requestTime; // 요청 시간 (yyyy-MM-dd'T'HH:mm:ss.SSS)
    private String statusCode;  // 요청 상태 코드 (202: 성공 | 그 외: 실패 | HTTP Status 규격을 따름)
    private String statusName;  // 요청 상태명 (success: 성공 | fail: 실패)
}
