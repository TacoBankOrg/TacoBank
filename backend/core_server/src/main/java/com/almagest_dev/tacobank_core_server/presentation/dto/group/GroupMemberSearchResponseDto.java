package com.almagest_dev.tacobank_core_server.presentation.dto.group;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GroupMemberSearchResponseDto {
    private Long memberId;
    private String memberName;
    private String status;
}
