package com.almagest_dev.tacobank_core_server.presentation.controller;

import com.almagest_dev.tacobank_core_server.application.service.MemberService;
import com.almagest_dev.tacobank_core_server.common.dto.CoreResponseDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.member.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/taco/core/members")
public class MemberController {
    private final MemberService memberService;

    /**
     * ID로 특정 Member 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> findMemberById(@PathVariable Long id) {
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "사용자 조회 성공", memberService.findMemberById(id)));
    }

    /**
     * 이메일 찾기
     */
    @PostMapping("/email-recovery")
    public ResponseEntity<?> findMemberEmailByTel(@RequestBody @Valid FindEmailRequestDto requestDto) {
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "이메일 찾기 성공", memberService.findEmailByTel(requestDto)));
    }

    /**
     * 회원 전화번호 수정
     */
    @PutMapping("/{id}/tel")
    public ResponseEntity<?> updateMemberTel(@PathVariable Long id, @RequestBody UpdateMemberRequestDto requestDto) {
        memberService.updateMemberTel(id, requestDto);
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "회원 정보가 성공적으로 수정되었습니다."));
    }

    /**
     * 회원 탈퇴
     */
    @PutMapping("/{id}/deactivation")
    public ResponseEntity<?> deleteMember(@PathVariable Long id) {
        memberService.deactivateMember(id);

        // 쿠키 삭제를 위한 ResponseCookie 생성
        ResponseCookie cookie = ResponseCookie.from("Authorization", "")
                .path("/")
                .httpOnly(true)
                .maxAge(0) // 쿠키 만료
                .build();

        // 응답에 쿠키 추가
        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString()) // Set-Cookie 헤더 추가
                .body(new CoreResponseDto<>("SUCCESS", "회원 탈퇴가 완료되었습니다."));
    }

    /**
     * 비밀번호 찾기 : 본인 인증 & 인증번호 발송
     */
    @PostMapping("/password-recovery/verify")
    public ResponseEntity<?> findMemberPasswordAndSendSmsAuth(@RequestBody @Valid FindPasswordRequestDto requestDto) {
        FindPasswordResponseDto response = memberService.findPasswordAndSendSmsAuth(requestDto);
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "인증 번호를 발송했습니다.", response));
    }
    /**
     * 비밀번호 찾기 : 새로운 비밀번호로 설정(본인 인증 후)
     */
    @PostMapping("/password-recovery/confirm")
    public ResponseEntity<?> confirmMemberPassword(@RequestBody @Valid ConfirmPasswordRequestDto requestDto) {
        memberService.confirmPassword(requestDto);
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "비밀번호 재설정이 완료되었습니다."));
    }

    /**
     * 비밀번호 재설정
     * - 기존 비밀번호, 새로운 비밀번호, 확인용 비밀번호
     */
    @PutMapping("/password")
    public ResponseEntity<?> resetMemberPassword(@RequestBody @Valid ResetPasswordRequestDto requestDto) {
        memberService.resetMemberPassword(requestDto);
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "비밀번호 재설정이 완료되었습니다."));
    }

    /**
     * 출금 비밀번호 설정
     */
    @PostMapping("/pin")
    public ResponseEntity<?> createTransferPin(@RequestBody @Valid CreatePinRequestDto requestDto) {
        memberService.createTransferPin(requestDto);
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "출금 비밀번호가 성공적으로 설정되었습니다."));
    }

    /**
     * 현재 출금 비밀번호 확인
     */
    @PostMapping("/pin-validate")
    public ResponseEntity<?> validateTransferPin(@RequestBody @Valid ValidatePinRequestDto requestDto) {
        memberService.validateTransferPin(requestDto);
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "출금 비밀번호 검증이 성공했습니다."));
    }

    /**
     * 출금 비밀번호 수정
     */
    @PostMapping("/pin-reset")
    public ResponseEntity<?> resetTransferPin(@RequestBody @Valid ChangePinRequestDto requestDto) {
        memberService.resetTransferPin(requestDto);
        return ResponseEntity.ok(new CoreResponseDto<>("SUCCESS", "출금 비밀번호가 성공적으로 변경되었습니다."));
    }

    /**
     * 이메일로 친구 검색
     */
    @GetMapping("/search/{email}")
    public ResponseEntity<CoreResponseDto<MemberSearchResponseDto>> searchMemberByEmail(
            @PathVariable String email,
            @RequestParam Long memberId // 현재 사용자의 ID를 요청 파라미터로 받음
    ) {
        // 서비스 호출
        MemberSearchResponseDto memberInfo = memberService.searchMemberByEmail(email, memberId);

        // CoreResponseDto를 반환
        return ResponseEntity.ok(
                new CoreResponseDto<>(
                        "SUCCESS",
                        "회원 검색 성공",
                        memberInfo
                )
        );
    }
}
