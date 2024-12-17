package com.almagest_dev.tacobank_core_server.presentation.dto.transantion;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDto {
    private Long accountId;         // 계좌 식별자
    private String accountName;     // 계좌명
    private String accountNum;      // 계좌 번호
    private String accountHolder;   // 예금주
    private String bankName;        // 은행 이름
    private String bankCode;        // 은행 코드
    private String balance;         // 잔액
    private List<TransactionDetails> transactionList;
}
