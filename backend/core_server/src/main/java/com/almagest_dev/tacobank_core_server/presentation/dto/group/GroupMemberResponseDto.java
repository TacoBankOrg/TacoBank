package com.almagest_dev.tacobank_core_server.presentation.dto.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberResponseDto {
    private Long groupId;
    private Long memberId;
    private String status;
    private String groupName;
}
