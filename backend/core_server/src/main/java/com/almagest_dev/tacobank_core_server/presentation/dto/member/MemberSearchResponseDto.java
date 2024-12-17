package com.almagest_dev.tacobank_core_server.presentation.dto.member;


import lombok.Data;

@Data
public class MemberSearchResponseDto {
    private Long memberId;  // Member ID
    private String name;    // Member 이름
    private String email;   // Member 이메일
    private String status;

    // 기본 생성자
    public MemberSearchResponseDto() {}

    // 모든 필드를 초기화하는 생성자
    public MemberSearchResponseDto(Long memberId, String name, String email, String status) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.status = status;
    }

}