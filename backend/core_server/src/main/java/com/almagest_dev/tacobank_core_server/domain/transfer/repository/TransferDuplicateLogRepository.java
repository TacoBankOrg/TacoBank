package com.almagest_dev.tacobank_core_server.domain.transfer.repository;

import com.almagest_dev.tacobank_core_server.domain.transfer.model.TransferDuplicateLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferDuplicateLogRepository extends JpaRepository<TransferDuplicateLog, Long> {
}
