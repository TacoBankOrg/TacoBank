package com.almagest_dev.tacobank_core_server.presentation.dto.transfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransferRequestDto {
    @NotBlank(message = "중복 방지 키를 보내주세요.")
    private String idempotencyKey;          // 중복 방지 키

    @NotNull(message = "사용자 ID를 입력해주세요.")
    private Long memberId; // 출금 사용자 ID

    private Long settlementId; // 정산 ID

    @NotNull(message = "출금 정보가 없습니다.")
    private WithdrawalDetails withdrawalDetails;

    @NotNull(message = "입금 정보가 없습니다.")
    private ReceiverDetails receiverDetails;

    @NotBlank(message = "출금 비밀번호를 입력해주세요.")
    private String transferPin;

    @NotNull(message = "송금액을 보내주세요.")
    private int amount;                 // 송금액

    private String rcvPrintContent;     // 입금계좌 인자내역
    private String wdPrintContent;      // 출금계좌 인자내역
}
