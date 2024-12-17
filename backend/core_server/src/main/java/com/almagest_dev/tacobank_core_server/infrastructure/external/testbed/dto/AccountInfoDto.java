package com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.dto;

import lombok.Data;

@Data
public class AccountInfoDto {

    private String bankCodeStd;
    private String fintechUseNum;
    private String activityType;
    private String accountType;
    private String accountNum;
    private String accountSeq;
    private String accountLocalCode;
    private String accountIssueDate;
    private String maturityDate;
    private String lastTranDate;
    private String productName;
    private String productSubName;
    private String dormancyYn;
    private String balanceAmt;
    private String accountHolder;

}
// 계좌정보 DTO