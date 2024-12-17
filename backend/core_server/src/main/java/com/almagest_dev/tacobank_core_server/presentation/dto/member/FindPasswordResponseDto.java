package com.almagest_dev.tacobank_core_server.presentation.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindPasswordResponseDto {
    private long memberId; // 멤버 ID
    private long verificationId; // 문자 인증 로그 ID
}
