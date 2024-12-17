package com.almagest_dev.tacobank_core_server.presentation.controller;

import com.almagest_dev.tacobank_core_server.application.service.AuthService;
import com.almagest_dev.tacobank_core_server.common.dto.CoreResponseDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.auth.SmsConfirmRequestDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.auth.SmsVerificationRequestDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.auth.SmsVerificationResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/taco/core/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * 문자 인증 요청
     */
    @PostMapping("/sms/verification")
    public ResponseEntity<?> sendSmsVerificationCode(@RequestBody @Valid SmsVerificationRequestDto requestDto) {
        SmsVerificationResponseDto response = authService.sendSmsVerificationCode(requestDto);
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "인증 번호를 발송했습니다.", response));

    }

    /**
     * 문자 인증 검증
     */
    @PutMapping("/sms/verification")
    public ResponseEntity<?> confirmSmsVerificationCode(@RequestBody @Valid SmsConfirmRequestDto requestDto) {
        authService.confirmSmsVerificationCode(requestDto);
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "본인 인증이 성공적으로 완료되었습니다."));
    }

}
