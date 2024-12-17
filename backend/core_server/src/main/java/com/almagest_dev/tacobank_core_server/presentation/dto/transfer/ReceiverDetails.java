package com.almagest_dev.tacobank_core_server.presentation.dto.transfer;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiverDetails {
    @NotBlank(message = "입금 계좌 번호를 입력해주세요.")
    private String accountNum; // 입금 계좌 번호

    @NotBlank(message = "입금 예금주를 입력해주세요.")
    private String accountHolder; // 입금 예금주

    @NotBlank(message = "입금 은행 코드를 입력해주세요.")
    private String bankCode; // 입금 은행 코드
}
