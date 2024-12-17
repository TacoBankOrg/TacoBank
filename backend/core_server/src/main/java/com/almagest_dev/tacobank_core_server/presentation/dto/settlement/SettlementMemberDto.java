package com.almagest_dev.tacobank_core_server.presentation.dto.settlement;

import lombok.Data;

@Data
public class SettlementMemberDto {
    private long memberId; // 멤버 ID
    private long amount; // 멤버별 금액
}
