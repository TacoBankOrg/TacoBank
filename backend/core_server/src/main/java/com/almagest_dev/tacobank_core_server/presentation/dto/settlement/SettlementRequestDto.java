package com.almagest_dev.tacobank_core_server.presentation.dto.settlement;

import com.almagest_dev.tacobank_core_server.presentation.dto.receipt.ProductMemberDetails;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SettlementRequestDto {
    @NotBlank(message = "정산 요청 종류가 없습니다.")
    private String type;        // 정산 종류 (일반: general | 영수증: receipt)
    private Long groupId;       // 선택된 그룹 ID (null이면 친구 선택)

    @NotNull(message = "정산 요청자 정보가 없습니다.")
    private Long leaderId;      // 그룹장 ID

    @NotNull(message = "정산액 정보가 없습니다.")
    @Min(value = 10, message = "정산액이 유효하지 않습니다.")
    private long totalAmount;    // 정산 총액

    private List<Long> friendIds;       // 친구 선택 시 친구 ID 목록

    @NotNull(message = "정산 받을 계좌정보가 없습니다.")
    private Long settlementAccountId;   // 선택된 계좌 ID
    @NotNull(message = "정산 멤버 정보가 없습니다.")
    private List<SettlementMemberDto> memberAmounts; // 멤버별 금액 리스트

    // 영수증 정산인 경우
    private Long receiptId; // 영수증 ID
    private List<ProductMemberDetails> productMemberDetails; // 영수증 품목별 포함된 멤버 정보 리스트
}
