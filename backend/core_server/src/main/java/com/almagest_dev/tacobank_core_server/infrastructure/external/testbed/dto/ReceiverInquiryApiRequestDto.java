package com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReceiverInquiryApiRequestDto {
    private String reqUserFinanceId;    // 요청 고객 사용자 금융 식별번호
    private String reqFintechUseNum;    // 요청 계좌 식별번호
    private String reqClientName;       // 요청 고객 성명
    private String bankCodeStd;         // 입금 은행 코드
    private String accountNum;          // 입금 계좌번호
    private String printContent;        // 입금 계좌 인자 내역
    private String tranAmt;             // 거래 금액
}
// 수취인 조회