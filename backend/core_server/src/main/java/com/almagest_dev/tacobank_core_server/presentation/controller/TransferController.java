package com.almagest_dev.tacobank_core_server.presentation.controller;

import com.almagest_dev.tacobank_core_server.application.service.TransferService;
import com.almagest_dev.tacobank_core_server.common.dto.CoreResponseDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.transfer.ReceiverInquiryRequestDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.transfer.TransferRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/taco/core/transfers")
public class TransferController {
    private final TransferService transferService;

    /**
     * 송금할 은행, 계좌번호 입력 후 수취인 조회
     */
    @PostMapping("/receiver")
    public ResponseEntity<?> inquireReceiverAccount(@RequestBody @Valid ReceiverInquiryRequestDto requestDto) {
        return ResponseEntity.ok(
                new CoreResponseDto<>("SUCCESS", "수취인 조회 성공", transferService.inquireReceiverAccount(requestDto))
        );
    }

    /**
     * 출금(이체)시 비밀번호 검증
     */
//    @PostMapping("/password/verify")
//    public ResponseEntity<?> validateTransferPassword(@RequestBody @Valid TransferRequestDto requestDto) {
//        transferService.verifyPassword(requestDto);
//        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "비밀번호 검증 성공", null));
//    }

    /**
     * 송금: 출금 비밀번호 검증 + 송금
     */
    @PostMapping
    public ResponseEntity<?> transfer(@RequestBody @Valid TransferRequestDto requestDto) {
        return ResponseEntity.ok(transferService.transfer(requestDto));
    }

}
