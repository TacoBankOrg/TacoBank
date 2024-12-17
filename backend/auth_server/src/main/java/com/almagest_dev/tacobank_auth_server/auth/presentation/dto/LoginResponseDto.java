package com.almagest_dev.tacobank_auth_server.auth.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class LoginResponseDto {
    private long memberId;          // 멤버 ID
    private String mydataLinked;    // 최초 계좌 연동 여부
}
