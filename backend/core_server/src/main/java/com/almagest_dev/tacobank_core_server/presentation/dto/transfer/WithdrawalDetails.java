package com.almagest_dev.tacobank_core_server.presentation.dto.transfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawalDetails {
    @NotNull(message = "출금 계좌 ID를 입력해주세요.")
    private Long accountId; // 출금 계좌 ID

    @NotBlank(message = "출금 계좌 번호를 입력해주세요.")
    private String accountNum; // 출금 계좌 번호

    @NotBlank(message = "출금 예금주 정보를 입력해주세요.")
    private String accountHolder; // 출금 예금주

    @NotBlank(message = "출금 은행 코드를 입력해주세요.")
    private String bankCode; // 출금 은행 코드
}
