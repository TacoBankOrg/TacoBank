package com.almagest_dev.tacobank_core_server.infrastructure.sms.dto;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerificationDataDto {
    private Long logId;
    private String requestId;
    private String verificationCode;
    private Long memberId;
}