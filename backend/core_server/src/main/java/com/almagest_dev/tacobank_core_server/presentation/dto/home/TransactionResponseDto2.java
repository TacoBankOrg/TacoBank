package com.almagest_dev.tacobank_core_server.presentation.dto.home;

import lombok.Data;

@Data
public class TransactionResponseDto2 {
    private String tranNum;
    private String printContent; // 입금출력내용
    private String tranDateTime; // 거래 일시 (YYYY.MM.DD HH:mm:ss)
    private String tranAmt;
    private String inoutType;

}
