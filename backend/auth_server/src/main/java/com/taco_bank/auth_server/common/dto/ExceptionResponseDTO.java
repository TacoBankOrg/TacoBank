package com.taco_bank.auth_server.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExceptionResponseDTO {
    private String error;
    private String message;
}
