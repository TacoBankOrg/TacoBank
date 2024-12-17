package com.almagest_dev.tacobank_core_server.presentation.dto.home;

import lombok.Data;

import java.util.List;

@Data
public class AccountMemberReponseDto {
    private Long memberId;
    private String email;
    private String name;
    private String tel;
    private Long mainAccountId;
    private boolean pinSet;
    private String mydataLinked;
    private List<AccountResponseDto> accountList;
}
