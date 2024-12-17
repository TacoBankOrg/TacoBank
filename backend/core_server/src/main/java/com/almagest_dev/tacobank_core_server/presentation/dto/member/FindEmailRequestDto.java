package com.almagest_dev.tacobank_core_server.presentation.dto.member;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class FindEmailRequestDto {
    @NotBlank(message = "전화번호를 입력해주세요.")
    private String tel; // 전화번호
}
