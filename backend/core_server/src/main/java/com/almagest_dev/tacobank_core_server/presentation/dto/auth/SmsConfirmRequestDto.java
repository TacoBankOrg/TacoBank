package com.almagest_dev.tacobank_core_server.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class SmsConfirmRequestDto {
    @NotBlank(message = "요청 타입을 보내주세요.")
    private String type; // 요청 타입 (회원가입: join | 비밀번호: pw | 출금 비밀번호: pin | 통합계좌연결: mydata | 휴대전화번호 수정: tel)

    private Long memberId; // 사용자 정보(로그인 한 경우)가 있는 경우에는 확인

    @NotNull(message = "인증 정보가 없습니다.")
    private Long verificationId;

    @NotBlank(message = "전화번호가 없습니다.")
    private String tel;

    @NotBlank(message = "인증번호를 입력해주세요.")
    @Pattern(regexp = "^\\d{6}$", message = "인증번호는 6자리이어야 합니다.")
    private String inputCode;
}
