package com.almagest_dev.tacobank_core_server.presentation.dto.transfer;

import com.almagest_dev.tacobank_core_server.presentation.dto.account.Account2Dto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TransferOptionsResponseDto {
    private List<Account2Dto> favoriteAccounts;  // 즐겨찾는 계좌 리스트
    private List<Account2Dto> recentAccounts;    // 최근 이체 계좌 리스트
    private List<Account2Dto> friendsAccounts;   // 친구 메인 계좌 리스트
}