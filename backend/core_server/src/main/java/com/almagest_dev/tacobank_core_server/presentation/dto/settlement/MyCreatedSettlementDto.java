package com.almagest_dev.tacobank_core_server.presentation.dto.settlement;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class MyCreatedSettlementDto {
    private Long settlementId;
    private Long groupId;
    private String groupName;
    private Long totalAmount;
    private String totalStatus;
    private LocalDateTime createdDate;
    private LocalDateTime completedDate;
}
