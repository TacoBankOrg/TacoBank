package com.almagest_dev.tacobank_core_server.presentation.dto.account;

import lombok.Data;

@Data
public class MainAccountRequestDto {
    private Long memberId;      // 사용자 ID
    private Long accountId;     // 선택한 계좌 ID
}
