package com.taco_bank.auth_server.auth.application.controller;

import com.taco_bank.auth_server.auth.application.dto.SignupRequestDTO;
import com.taco_bank.auth_server.auth.application.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/members")
    public ResponseEntity<?> register(@RequestBody @Valid SignupRequestDTO requestDTO) {
        // 회원 등록
        authService.registerMember(requestDTO);
        return ResponseEntity.ok("회원가입이 성공적으로 완료되었습니다!");
    }

}
