package com.almagest_dev.tacobank_core_server.presentation.dto.member;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindEmailResponseDto {
    private String email;
}
