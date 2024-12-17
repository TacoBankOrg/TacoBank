package com.almagest_dev.tacobank_core_server.common.utils;

import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;


@Component
public class MaskingUtil {
    /**
     * 전화번호 마스킹 (예: 010-1234-5678 -> 010-****-5678)
     */
    public static String maskPhoneNumber(String phoneNumber) {
        if (StringUtils.isBlank(phoneNumber) || phoneNumber.length() < 4) {
            return "****";
        }
        return phoneNumber.replaceAll("(\\d{3})-(\\d{2,4})-(\\d{4})", "$1-****-$3");
    }

    /**
     * 계좌번호 마스킹 | 뒤에서부터 5자리 (예: 123456789012 -> 1234567*****)
     */
    public static String maskAccountNumber(String accountNumber) {
        if (StringUtils.isBlank(accountNumber) || accountNumber.length() < 5) {
            return "****";
        }
        return accountNumber.substring(0, accountNumber.length() - 5) + "*****";
    }

    /**
     * 이름 마스킹 (예: 홍길동 -> 홍*동)
     */
    public static String maskName(String name) {
        if (StringUtils.isBlank(name) || name.length() < 2) {
            return "***";
        }
        return name.substring(0, 1) + "*".repeat(name.length() - 2) + name.substring(name.length() - 1);
    }

    /**
     * 이메일 마스킹 (예: test123@gmail.com -> tes****@gmail.com)
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "*****";
        }

        // 이메일 ID와 도메인 분리
        String[] emailParts = email.split("@");
        String id = emailParts[0];
        String domain = emailParts[1];

        // ID가 3자 미만인 경우 처리
        if (id.length() <= 3) {
            // 맨 뒤 한 글자만 마스킹
            return id.substring(0, id.length() - 1) + "*" + "@" + domain;
        }

        // ID 앞 3글자는 유지, 나머지는 *로 마스킹
        String visiblePart = id.substring(0, 3); // 앞 3글자
        String maskedPart = "*".repeat(id.length() - 3); // 나머지 글자 수만큼 * 생성

        return visiblePart + maskedPart + "@" + domain;
    }

    /**
     * 기본 마스킹 (전체 문자 * 처리)
     */
    public static String maskAll(String input) {
        if (StringUtils.isBlank(input)) {
            return "****";
        }
        return "*".repeat(input.length());
    }
}
