package com.almagest_dev.tacobank_core_server.presentation.dto.transantion;

import lombok.Data;

@Data
public class TransactionDetails {
    private String tranNum; // 거래 고유 시퀀스 번호
    private String type; // 거래 타입
    private String printContent; // 입금출력내용
    private Double afterBalanceAmount; // 거래 후 잔액
    private String tranDateTime; // 거래 일시 (YYYY.MM.DD HH:mm:ss)
    private String tranAmt;
    private String inoutType;
}
