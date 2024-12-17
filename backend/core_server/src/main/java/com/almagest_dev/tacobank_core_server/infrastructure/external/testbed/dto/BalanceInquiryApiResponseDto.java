package com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.dto;

import lombok.Data;

@Data
public class BalanceInquiryApiResponseDto {
    private String apiTranId;         // API 거래 ID
    private String apiTranDtm;        // API 거래 일시
    private String rspCode;           // 응답 코드
    private String rspMessage;        // 응답 메시지
    private String bankCodeTran;      // 은행 코드 (거래 기준)
    private String bankName;          // 은행 이름
    private String fintechUseNum;     // 핀테크 이용 번호
    private String balanceAmt;        // 잔액
    private String availableAmt;      // 사용 가능 금액
    private String accountType;       // 계좌 유형
    private String productName;       // 상품 이름
    private String accountIssueDate;  // 계좌 개설일
    private String lastTranDate;      // 마지막 거래일
    private AccountInfo accountInfo;  // 계좌 상세 정보

    @Data
    public static class AccountInfo {
        private String fintechUseNum;     // 핀테크 이용 번호
        private String bankCodeStd;       // 은행 코드 (표준)
        private String activityType;      // 활동 유형
        private String accountType;       // 계좌 유형
        private String accountNum;        // 계좌 번호
        private String accountSeq;        // 계좌 순번
        private String accountIssueDate;  // 계좌 개설일
        private String maturityDate;      // 만기일
        private String lastTranDate;      // 마지막 거래일
        private String productName;       // 상품 이름
        private String productSubName;    // 상품 부이름
        private String dormancyYn;        // 휴면 여부
        private String balanceAmt;        // 잔액
    }
}
