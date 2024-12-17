package com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.dto;


import lombok.Data;

@Data
public class TransactionDetailApiDto {
    private String tranDate;
    private String tranTime;
    private String inoutType;
    private String tranType;
    private String printContent;
    private String tranAmt;
    private String tranNum;
    private String afterBalanceAmt;

}
//거래 내역 상세 정보 DTO