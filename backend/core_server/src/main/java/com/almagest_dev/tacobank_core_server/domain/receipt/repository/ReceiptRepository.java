package com.almagest_dev.tacobank_core_server.domain.receipt.repository;

import com.almagest_dev.tacobank_core_server.domain.receipt.model.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
}
