package com.almagest_dev.tacobank_core_server.presentation.controller;

import com.almagest_dev.tacobank_core_server.application.service.ReceiptOcrService;
import com.almagest_dev.tacobank_core_server.common.dto.CoreResponseDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.receipt.ReceiptOcrResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/taco/core/receipts")
@RequiredArgsConstructor
public class ReceiptOcrController {
    private final ReceiptOcrService receiptOcrService;

    @PostMapping("/ocr")
    public ResponseEntity<?> extractDataFromReceiptImage(@RequestParam("file") MultipartFile file) {
        ReceiptOcrResponseDto response = receiptOcrService.processReceiptOcr(file);
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "OCR 추출 성공", response));
    }

    @PostMapping("/{receiptId}/products/members")
    private ResponseEntity<?> assignMembersToReceiptProducts(@PathVariable Long receiptId) {
        return null;
    }
}
