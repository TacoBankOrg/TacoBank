package com.almagest_dev.tacobank_core_server.presentation.dto.account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountInfoWithBalance {
    private Long accountId;     // 계좌 ID
    private String accountNum;  // 계좌번호
    private String bankCode;    // 은행코드
    private String bankName;    // 은행명
    private int balance;        // 계좌 잔액
}