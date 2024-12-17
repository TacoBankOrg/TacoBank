package com.almagest_dev.tacobank_core_server.domain.settlememt.repository;


import com.almagest_dev.tacobank_core_server.domain.settlememt.model.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByPayGroup_Leader_Id(Long leaderId);

    Optional<Settlement> findByIdAndSettlementStatus(Long settlementId, String settlementStatus);
}
