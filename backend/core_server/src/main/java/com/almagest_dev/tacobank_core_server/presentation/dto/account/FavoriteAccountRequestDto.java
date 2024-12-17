package com.almagest_dev.tacobank_core_server.presentation.dto.account;

import lombok.Data;

@Data
public class FavoriteAccountRequestDto {
    private Long memberId; // 사용자 ID
    private String accountNum; // 계좌 번호

}
