package com.almagest_dev.tacobank_core_server.presentation.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class SmsVerificationRequestDto {
    @NotBlank(message = "요청 종류가 없습니다.")
    private String type; // 요청 타입 (회원가입: join | 비밀번호: pw | 출금 비밀번호: pin | 통합계좌연결: mydata | 휴대전화번호 수정: tel)
    private Long memberId; // 사용자 정보(로그인 한 경우)가 있는 경우에는 확인

    @NotBlank(message = "전화번호를 입력해주세요.")
    private String tel;
}
