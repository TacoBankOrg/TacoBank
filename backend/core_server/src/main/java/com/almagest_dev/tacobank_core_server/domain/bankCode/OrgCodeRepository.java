package com.almagest_dev.tacobank_core_server.domain.bankCode;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrgCodeRepository extends JpaRepository<OrgCode, Long> {
    Optional<OrgCode> findByCode(String code);
}
