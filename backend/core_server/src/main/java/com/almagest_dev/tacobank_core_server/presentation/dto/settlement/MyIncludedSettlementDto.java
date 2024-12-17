package com.almagest_dev.tacobank_core_server.presentation.dto.settlement;


import com.almagest_dev.tacobank_core_server.presentation.dto.account.AccountDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MyIncludedSettlementDto {
    private Long settlementId;
    private LocalDateTime createdDate;
    private Long memberAmount;
    private String memberStatus;
    private AccountDto account;
    private Long leaderId;
}
