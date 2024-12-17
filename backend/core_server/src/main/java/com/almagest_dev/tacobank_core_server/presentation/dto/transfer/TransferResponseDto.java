package com.almagest_dev.tacobank_core_server.presentation.dto.transfer;

import com.almagest_dev.tacobank_core_server.common.utils.MaskingUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TransferResponseDto {
    private String idempotencyKey;          // 중복 방지 키
    private String tranDtm;                 // 송금 일시
    private Long memberId;                  // 송금하는 사용자 ID (출금 사용자)

    private WithdrawalDetails withdrawalDetails;    // 출금 계좌 정보
    private ReceiverDetails receiverDetails;        // 입금 계좌 정보

    private int amount;                 // 송금액

    public static TransferResponseDto create(
            String idempotencyKey,
            String apiTranDtm,
            Long memberId,
            Long withdrawalAccountId,
            String withdrawalAccountNum,
            String withdrawalAccountHolder,
            String withdrawalBankCode,
            String receiverAccountNum,
            String receiverAccountHolder,
            String receiverBankCode,
            int amount
    ) {
        // 포맷 변경
        String formattedTranDtm = LocalDateTime.parse(apiTranDtm.substring(0, 14), // 초 단위까지만 파싱
                        DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        return new TransferResponseDto(
                idempotencyKey,
                formattedTranDtm,
                memberId,
                new WithdrawalDetails(
                        withdrawalAccountId,
                        MaskingUtil.maskAccountNumber(withdrawalAccountNum),
                        MaskingUtil.maskName(withdrawalAccountHolder),
                        withdrawalBankCode
                ),
                new ReceiverDetails(
                        MaskingUtil.maskAccountNumber(receiverAccountNum),
                        MaskingUtil.maskName(receiverAccountHolder),
                        receiverBankCode
                ),
                amount
        );
    }
}
