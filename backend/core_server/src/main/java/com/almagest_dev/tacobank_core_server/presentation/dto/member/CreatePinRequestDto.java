package com.almagest_dev.tacobank_core_server.presentation.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreatePinRequestDto {
    @NotNull(message = "회원 정보가 없습니다..")
    private Long memberId;

    @NotBlank(message = "출금 비밀번호를 입력해주세요.")
    @Pattern(regexp = "^\\d{6}$", message = "출금 비밀번호는 6자리 숫자여야 합니다.")
    private String transferPin;

    @NotBlank(message = "확인 출금 비밀번호를 입력해주세요.")
    @Pattern(regexp = "^\\d{6}$", message = "출금 비밀번호는 6자리 숫자여야 합니다.")
    private String confirmTransferPin;
}
