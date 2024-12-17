package com.almagest_dev.tacobank_core_server.domain.receipt.repository;

import com.almagest_dev.tacobank_core_server.domain.receipt.model.ReceiptOcrLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptOcrLogRepository extends JpaRepository<ReceiptOcrLog, Long> {
}
