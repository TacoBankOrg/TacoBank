package com.almagest_dev.tacobank_core_server.presentation.dto.group;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GroupSearchResponseDto {
    private Long groupId;
    private String groupName;
    private List<GroupMemberSearchResponseDto> members;
}
