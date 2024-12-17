package com.almagest_dev.tacobank_core_server.presentation.dto.group;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GroupRequestDto {
    private Long leaderId;
    private String groupName;
    private String customized;
    private List<Long> friendIds;
}
