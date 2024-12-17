package com.almagest_dev.tacobank_auth_server.auth.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignupRequestDTO {
    @NotBlank(message = "이메일은 필수 값입니다.")
    @Email(message = "올바른 이메일 형식으로 입력해주세요.")
    private String email;

    @NotBlank(message = "이름은 필수 값입니다.")
    @Size(max = 20, message = "이름은 20자 이하여야 합니다.")
    private String name;

    @NotBlank(message = "생년월일은 필수 값입니다.")
    @Size(min= 6, max = 8, message = "생년월일은 6자리이어야 합니다.")
    private String birth;

    @NotBlank(message = "비밀번호는 필수 값입니다.")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "전화번호는 필수 값입니다.")
    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다.")
    private String tel;
}
