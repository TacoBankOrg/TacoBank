package com.almagest_dev.tacobank_core_server.presentation.dto.member;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class FindPasswordRequestDto {
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;
    @NotBlank(message = "전화번호를 입력해주세요.")
    private String tel;
}
