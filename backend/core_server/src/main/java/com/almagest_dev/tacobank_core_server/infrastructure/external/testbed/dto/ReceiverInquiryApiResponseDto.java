package com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.dto;

import lombok.Data;

@Data
public class ReceiverInquiryApiResponseDto {
    private String apiTranId;                   // API 거래 고유 번호 | "AA12349BHZ1324K82AL3"
    private String apiTranDtm;                  // 거래 일시
    private String rspCode;                     // 응답 코드 | "A0000"
    private String rspMessage;                  // 응답 메시지
    private String bankCodeStd;                 // 입금 기관 코드
    private String bankName;                    // 입금 기관명
    private String accountNum;                  // 입금 계좌 번호
    private String accountNumMasked;            // 입금 계좌 번호 (출력용) | "300123-0000-***"
    private String printContent;                // 입금 계좌 인자 내역 - 수취인 계좌에 표시될 정보
    private String accountHolderName;           // 수취인 성명
    private String recvAccountFintechUseNum;    // 수취 조회 계좌 고유 번호 - 계좌 UUID
    private String wdBankCodeStd;               // 출금 (개설) 기관 표준 코드
    private String wdBankName;                  // 출금 (개설) 기관명
    private String wdAccountNum;                // 출금 계좌 번호
    private String tranAmt;                     // 거래 금액
}
