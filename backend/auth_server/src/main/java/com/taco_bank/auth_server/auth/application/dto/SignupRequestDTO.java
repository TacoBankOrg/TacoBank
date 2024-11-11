package com.taco_bank.auth_server.auth.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignupRequestDTO {
    @NotBlank(message = "이메일은 필수 값입니다.")
    @Email(message = "올바른 이메일 형식으로 입력해주세요.")
    private String email;

    @NotBlank(message = "이름은 필수 값입니다.")
    private String name;

    @NotBlank(message = "비밀번호는 필수 값입니다.")
    @Min(value = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "전화번호는 필수 값입니다.")
    private String tel;
}
