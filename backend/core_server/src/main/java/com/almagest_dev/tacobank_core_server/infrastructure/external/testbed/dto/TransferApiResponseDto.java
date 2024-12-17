package com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.dto;

import lombok.Data;

@Data
public class TransferApiResponseDto {
    private String apiTranId;               // API 거래 ID
    private String apiTranDtm;              // API 거래 일시
    private String tranAmt;                 // 거래 금액
    private String wdLimitRemainAmt;        // 출금 한도 잔액
    private String rspMessage;              // 응답 메시지
    private String tranResult;              // 거래 결과
    private String rspCode;                 // 응답 코드
    private String fintechUseNum;           // 핀테크 사용 번호
    private String accountAlias;            // 계좌 별칭
    private String bankCodeStd;             // 은행 코드
    private String bankName;                // 은행 이름
    private String accountNumMasked;        // 마스킹된 계좌 번호
    private String printContent;            // 출력 내용
    private String accountHolderName;       // 계좌 예금주 이름
    private String dpsAccountHolderName;    // 입금 계좌 예금주 이름
    private String dpsBankCodeStd;          // 입금 은행 코드
    private String dpsBankName;             // 입금 은행 이름
    private String dpsAccountNumMasked;     // 마스킹된 입금 계좌 번호
    private String dpsPrintContent;         // 입금 출력 내용
}
