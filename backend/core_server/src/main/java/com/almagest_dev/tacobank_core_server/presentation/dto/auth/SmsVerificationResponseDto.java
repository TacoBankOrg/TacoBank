package com.almagest_dev.tacobank_core_server.presentation.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SmsVerificationResponseDto {
    private long verificationId; // 문자 인증 로그 ID
}
