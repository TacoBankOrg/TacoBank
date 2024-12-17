package com.almagest_dev.tacobank_core_server.presentation.dto.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Account2Dto {
    private String accountHolder;
    private String accountNum;
    private String bankCode;
}
