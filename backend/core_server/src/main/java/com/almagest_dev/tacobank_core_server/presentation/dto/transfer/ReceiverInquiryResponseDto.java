package com.almagest_dev.tacobank_core_server.presentation.dto.transfer;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReceiverInquiryResponseDto {
    private String idempotencyKey;          // 중복 방지 키
    private String receiverAccountHolder;   // 입금 예금주(수취인명)

    private Long withdrawalAccountId;       // 출금 계좌 ID
    private String withdrawalAccountNum;    // 출금 계좌 번호
    private int withdrawalBalance;          // 출금 계좌 잔액
}
