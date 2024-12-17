package com.almagest_dev.tacobank_core_server.application.service;

import com.almagest_dev.tacobank_core_server.domain.bankCode.OrgCode;
import com.almagest_dev.tacobank_core_server.domain.bankCode.OrgCodeRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrgCodeService {

    private final OrgCodeRepository orgCodeRepository;

    public String getBankNameByCode(String code) {
        OrgCode orgCode = orgCodeRepository.findByCode(code).orElse(null);
        if (orgCode != null) {
            return orgCode.getName();
        }
        throw new IllegalArgumentException("Invalid bank code: " + code);
    }
}
