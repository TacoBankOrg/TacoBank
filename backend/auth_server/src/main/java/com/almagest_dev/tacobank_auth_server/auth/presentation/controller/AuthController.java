package com.almagest_dev.tacobank_auth_server.auth.presentation.controller;

import com.almagest_dev.tacobank_auth_server.auth.presentation.dto.DuplicateEmailRequestDto;
import com.almagest_dev.tacobank_auth_server.auth.presentation.dto.SignupRequestDTO;
import com.almagest_dev.tacobank_auth_server.auth.application.service.AuthService;
import com.almagest_dev.tacobank_auth_server.common.dto.AuthResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;

@RestController
@RequestMapping("/taco/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    /**
     * 회원가입
     */
    @PostMapping("/members")
    public ResponseEntity<?> register(@RequestBody @Valid SignupRequestDTO requestDTO) {
        authService.registerMember(requestDTO);
        return ResponseEntity.ok(new AuthResponseDto("SUCCESS", "회원가입이 성공적으로 완료되었습니다!"));
    }

    /**
     * 이메일 중복 확인
     */
    @PostMapping("/email")
    public ResponseEntity<?> checkDuplicateEmail(@RequestBody @Valid DuplicateEmailRequestDto requestDto) {
        authService.checkDuplicateEmail(requestDto);
        return ResponseEntity.ok(new AuthResponseDto("SUCCESS", "사용 가능한 이메일 입니다."));
    }

    /**
     * 세션 연장
     */
    @PostMapping("/extend-session")
    public ResponseEntity<?> extendSession(HttpServletRequest request, HttpServletResponse response) {
        // 새 토큰 발급
        String newToken = authService.extendSession(request.getCookies());

        // 쿠키 설정
        Cookie authorizationCookie = new Cookie("Authorization", newToken);
        authorizationCookie.setHttpOnly(true);
        authorizationCookie.setSecure(true); // HTTPS 환경에서는 true로 설정
        authorizationCookie.setMaxAge(60 * 10); // 10분
        authorizationCookie.setPath("/"); // 모든 경로에서 유효
        response.addCookie(authorizationCookie);

        return ResponseEntity.ok(new AuthResponseDto<>("SUCCESS", "세션 연장 성공"));
    }
}
