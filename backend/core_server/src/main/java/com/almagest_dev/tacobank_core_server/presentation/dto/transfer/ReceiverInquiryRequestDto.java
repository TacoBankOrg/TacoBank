package com.almagest_dev.tacobank_core_server.presentation.dto.transfer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReceiverInquiryRequestDto {
    @NotBlank(message = "송금 정보를 보내주세요.")
    private String idempotencyKey; // 중복 방지 키

    private Long settlementId; // 정산(바로 송금)인 경우 필수

    @NotNull(message = "회원 정보를 보내주세요.")
    private Long withdrawalMemberId;      // 출금 멤버 아이디

    @NotNull(message = "계좌 정보를 보내주세요.")
    private Long withdrawalAccountId;      // 출금 계좌 아이디

    @NotBlank(message = "은행(기관) 정보를 보내주세요.")
    private String receiverBankCode;    // 입금(수취) 은행/기관 코드

    @NotBlank(message = "계좌번호를 보내주세요.")
    private String receiverAccountNum;  // 입금(수취) 계좌번호
}
