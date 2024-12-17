package com.almagest_dev.tacobank_auth_server.auth.presentation.dto;

import lombok.Getter;

@Getter
public class LoginRequestDTO {
    private String email;
    private String password;
}
