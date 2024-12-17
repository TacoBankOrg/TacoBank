package com.almagest_dev.tacobank_core_server.presentation.dto.group;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponseDto {
    private Long groupId;
    private String groupName;
    private String customized;
    private String activated;
    private List<GroupMemberResponseDto> members;
}
