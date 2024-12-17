package com.almagest_dev.tacobank_core_server.domain.receipt.repository;

import com.almagest_dev.tacobank_core_server.domain.receipt.model.ReceiptMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptMemberRepository extends JpaRepository<ReceiptMember, Long> {
}
