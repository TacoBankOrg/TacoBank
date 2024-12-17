package com.almagest_dev.tacobank_core_server.presentation.dto.settlement;

import lombok.Data;
import java.util.List;

@Data
public class SettlementDetailsListResponseDto {
    private Long settlementId;
    private List<SettlementDetailsResponseDto2> settlementDetailsList;

    public SettlementDetailsListResponseDto(Long settlementId, List<SettlementDetailsResponseDto2> settlementDetailsList) {
        this.settlementId = settlementId;
        this.settlementDetailsList = settlementDetailsList;
    }
}
