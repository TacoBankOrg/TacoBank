package com.almagest_dev.tacobank_core_server.presentation.dto.group;

import lombok.Data;

import java.util.List;

@Data
public class MyGroupsResponseDto {
    private Long groupId;
    private String groupName;
    private String customized;
    private String activated;
    private String leaderName;
    private Long leaderId;
    private List<MemberInfo> members;

    @Data
    public static class MemberInfo {

        private Long groupId;
        private Long memberId;
        private String memberName;
        private String status;


    }

}
