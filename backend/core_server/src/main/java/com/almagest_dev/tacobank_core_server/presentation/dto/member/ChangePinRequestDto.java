package com.almagest_dev.tacobank_core_server.presentation.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePinRequestDto {
    @NotNull(message = "회원 정보가 없습니다.")
    private Long memberId;

    @NotBlank(message = "현재 비밀번호를 입력하세요.")
    @Pattern(regexp = "^\\d{6}$", message = "현재 비밀번호는 6자리 숫자여야 합니다.")
    private String currentPin;

    @NotBlank(message = "새 비밀번호를 입력하세요.")
    @Pattern(regexp = "^\\d{6}$", message = "비밀번호는 6자리 숫자여야 합니다.")
    private String newPin;

    @NotBlank(message = "새 비밀번호 확인을 입력하세요.")
    @Pattern(regexp = "^\\d{6}$", message = "비밀번호는 6자리 숫자여야 합니다.")
    private String confirmPin;
}
