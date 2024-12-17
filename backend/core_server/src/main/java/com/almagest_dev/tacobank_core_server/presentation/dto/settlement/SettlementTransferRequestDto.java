package com.almagest_dev.tacobank_core_server.presentation.dto.settlement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SettlementTransferRequestDto {
    // @TODO 사용자가 받을 메시지로 바꿔야 할 듯 (Back에서는 확인해야됨 '중복방지키가 없다'는 것을)
    @NotBlank(message = "중복 방지 키를 보내주세요.")
    private String idempotencyKey;          // 중복 방지 키

    @NotNull(message = "사용자 ID를 입력해주세요.")
    private Long memberId; // 출금 사용자 ID

    @NotNull(message = "정산 정보가 없습니다.")
    private Long settlementId;

    @NotNull(message = "정산 금액을 보내주세요.")
    private int amount;
}
