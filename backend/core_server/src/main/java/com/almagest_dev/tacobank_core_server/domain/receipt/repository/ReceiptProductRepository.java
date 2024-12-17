package com.almagest_dev.tacobank_core_server.domain.receipt.repository;

import com.almagest_dev.tacobank_core_server.domain.receipt.model.ReceiptProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptProductRepository extends JpaRepository<ReceiptProduct, Long> {
}
