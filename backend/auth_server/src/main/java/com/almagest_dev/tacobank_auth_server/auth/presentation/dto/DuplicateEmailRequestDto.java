package com.almagest_dev.tacobank_auth_server.auth.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class DuplicateEmailRequestDto {
    @NotBlank(message = "이메일은 필수 값입니다.")
    @Email(message = "올바른 이메일 형식으로 입력해주세요.")
    private String email;
}
