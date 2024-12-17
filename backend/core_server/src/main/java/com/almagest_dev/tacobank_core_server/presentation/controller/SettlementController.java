package com.almagest_dev.tacobank_core_server.presentation.controller;


import com.almagest_dev.tacobank_core_server.application.service.SettlementService;
import com.almagest_dev.tacobank_core_server.common.dto.CoreResponseDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.settlement.*;
import jakarta.validation.Valid;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Getter
@RestController
@RequestMapping("/taco/core/settlement")
public class SettlementController {

    private final SettlementService settlementService;
    public SettlementController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    // 송금 요청하기
    @PostMapping
    public ResponseEntity<?> requestSettlement(
            @RequestBody SettlementRequestDto request) {
        settlementService.processSettlementRequest(request);
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "송금 요청이 성공적으로 처리되었습니다."));
    }

    // 정산 현황 조회
    @GetMapping("/status/{memberId}")
    public ResponseEntity<CoreResponseDto<SettlementStatusResponseDto>> getSettlementStatus(
            @PathVariable Long memberId) {
        SettlementStatusResponseDto settlementStatus = settlementService.getSettlementStatus(memberId);
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "정산 현황 조회 성공", settlementStatus));
    }

    // 정산 상세내역 조회
    @GetMapping("/{settlementId}/details/{memberId}")
    public ResponseEntity<CoreResponseDto<SettlementDetailsListResponseDto>> getSettlementDetails(
            @PathVariable Long settlementId,
            @PathVariable Long memberId) {
        SettlementDetailsListResponseDto response = settlementService.getSettlementDetails(settlementId, memberId);
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "정산 상세내역 조회 성공", response));
    }

    // 독촉 알림 보내기
    @PostMapping("/{settlementId}/notify/{memberId}")
    public ResponseEntity<?> notifyPendingSettlementForMember(
            @PathVariable Long settlementId,
            @PathVariable Long memberId) {
        settlementService.notifyPendingSettlementForMember(settlementId, memberId);
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "독촉 알림이 성공적으로 전송되었습니다."));
    }

    // 바로 송금 - 정산 데이터 검증 & 사용자 전 계좌 잔액 조회
    @PostMapping("/transfers")
    public ResponseEntity<?> validateSettlementsAndGetAvailableBalances(
            @RequestBody @Valid SettlementTransferRequestDto requestDto) {
        SettlementTransferResponseDto response = settlementService.validateSettlementsAndGetAvailableBalances(requestDto);
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "사용자 출금 가능 계좌 잔액조회 성공", response));
    }

}
