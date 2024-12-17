package com.almagest_dev.tacobank_core_server.domain.transfer.repository;

import com.almagest_dev.tacobank_core_server.domain.transfer.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import java.util.List;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    boolean existsByIdempotencyKeyAndStatusIn(String idempotencyKey, List<String> status);

    // 최근 송금 성공 내역 5개 조회
    @Query("SELECT t FROM Transfer t WHERE t.memberId = :memberId AND t.status = :status GROUP BY t.receiverAccountNum")
    List<Transfer> findTop5DistinctByMemberIdAndStatus(Long memberId, String status, Pageable pageable);
}
