package com.almagest_dev.tacobank_core_server.presentation.dto.transantion;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionListRequestDto {
    @NotNull(message = "계좌 정보를 보내주세요.")
    private Long accountId;     // Account ID

    private String fromDate;    // 조회 시작 날짜
    private String toDate;      // 조회 종료 날짜
}
