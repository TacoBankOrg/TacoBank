package com.almagest_dev.tacobank_core_server.domain.settlememt.repository;
import com.almagest_dev.tacobank_core_server.domain.settlememt.model.SettlementDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SettlementDetailsRepository extends JpaRepository<SettlementDetails, Long> {
    @Query("SELECT sd FROM SettlementDetails sd WHERE sd.settlement.payGroup.id = :groupId")
    List<SettlementDetails> findByGroupId(Long groupId);


    // 특정 그룹과 사용자에 대한 정산 정보를 조회하는 메서드
    List<SettlementDetails> findByGroupMember_Member_IdAndGroupMember_PayGroup_Id(Long memberId, Long groupId);
    List<SettlementDetails> findByGroupMember_Member_Id(Long memberId);
    List<SettlementDetails> findBySettlement_Id(Long settlementId);
    List<SettlementDetails> findBySettlement_IdAndSettlementStatus(Long settlementId, String settlementStatus);

    Optional<SettlementDetails> findBySettlement_IdAndGroupMember_Member_IdAndSettlementStatus(
            Long settlementId, Long memberId, String settlementStatus);
    // 특정 정산의 특정 그룹 구성원 개별 정산 상세 정보
    Optional<SettlementDetails> findBySettlement_IdAndGroupMember_Id(Long settlementId, Long groupMemberId);

}