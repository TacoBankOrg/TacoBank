package com.almagest_dev.tacobank_core_server.domain.sms.repository;

import com.almagest_dev.tacobank_core_server.domain.sms.model.SmsVerificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SmsVerificationLogRepository extends JpaRepository<SmsVerificationLog, Long> {
    Optional<SmsVerificationLog> findByIdAndVerificationStatus(Long id, String verificationCode);
}
