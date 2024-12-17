package com.almagest_dev.tacobank_core_server.presentation.dto.settlement;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SettlementDetailsResponseDto2 {
    private Long groupMemberId;
    private String groupMemberName;
    private Long groupMemberAmount;
    private String groupMemberStatus;
    private LocalDateTime updatedDate;
    private Long leaderId;

    public SettlementDetailsResponseDto2(Long groupMemberId, String groupMemberName, Long groupMemberAmount, String groupMemberStatus, LocalDateTime updatedDate, Long leaderId) {
        this.groupMemberId = groupMemberId;
        this.groupMemberName = groupMemberName;
        this.groupMemberAmount = groupMemberAmount;
        this.groupMemberStatus = groupMemberStatus;
        this.updatedDate = updatedDate;
        this.leaderId= leaderId;
    }

}
