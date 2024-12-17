package com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BalanceInquiryApiRequestDto {
    private String userFinanceId;   // 요청 고객 사용자 금융 식별번호
    private String fintechUseNum;   // 요청 계좌 식별번호
    private String tranDtime;       // 요청 일시
}
// 잔액 조회
