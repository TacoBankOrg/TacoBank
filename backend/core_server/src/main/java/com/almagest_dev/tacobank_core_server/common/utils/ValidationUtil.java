package com.almagest_dev.tacobank_core_server.common.utils;

import org.springframework.stereotype.Component;

@Component
public class ValidationUtil {
    private static final String ALLOWED_SPECIAL_CHARACTERS = "!@_";

    /**
     * 비밀번호 규칙 검사
     */
    public static void validatePassword(String password, int minLen, String birthDate, String tel) {
        if (!isValidLength(password, minLen, 16)) {
            throw new IllegalArgumentException("비밀번호는 " + minLen + "자 이상 16자 이하이어야 합니다.");
        }
        if (!containsAllowedCharacters(password)) {
            throw new IllegalArgumentException("비밀번호는 허용되지 않은 문자를 포함할 수 없습니다.");
        }
        if (containsSensitiveInfo(password, birthDate, tel)) {
            throw new IllegalArgumentException("비밀번호에 생년월일 또는 전화번호를 포함할 수 없습니다.");
        }
        if (!containsRequiredTypes(password)) {
            throw new IllegalArgumentException("비밀번호에는 영문자, 숫자, 특수문자가 최소 1개 이상 포함되어야 합니다.");
        }
        if (hasRepeatedNumbers(password)) {
            throw new IllegalArgumentException("비밀번호에 동일한 숫자가 3번 이상 반복될 수 없습니다.");
        }
        if (hasSequentialNumbers(password)) {
            throw new IllegalArgumentException("비밀번호에 연속된 숫자가 포함될 수 없습니다.");
        }
    }

    /**
     * 출금 비밀번호 규칙 검사
     */
    public static void validateTransferPin(String pin) {
        if (pin == null || pin.isEmpty()) {
            throw new IllegalArgumentException("출금 비밀번호는 비어 있을 수 없습니다.");
        }
        if (!pin.matches("\\d+")) {
            throw new IllegalArgumentException("출금 비밀번호는 숫자만 포함해야 합니다.");
        }
        if (removeNonDigits(pin).length() == 0) {
            throw new IllegalArgumentException("출금 비밀번호에 숫자가 포함되어야 합니다.");
        }
        if (pin.length() != 6) {
            throw new IllegalArgumentException("출금 비밀번호는 6자리여야 합니다.");
        }
        if (hasRepeatedNumbers(pin)) {
            throw new IllegalArgumentException("출금 비밀번호에 동일한 숫자가 3번 이상 반복될 수 없습니다.");
        }
        if (hasSequentialNumbers(pin)) {
            throw new IllegalArgumentException("출금 비밀번호에 연속된 숫자가 포함될 수 없습니다.");
        }
    }

    /**
     * 길이 유효성 검사 (minLen 이상)
     */
    public static boolean isValidLength(String str, int minLen, int maxLen) {
        return str != null && str.length() >= minLen && str.length() <= maxLen;
    }

    /**
     * 영문자, 숫자, 허용 특수문자 이외 문자 포함시 False
     */
    public static boolean containsAllowedCharacters(String str) {
        return str.matches("^[a-zA-Z0-9" + ALLOWED_SPECIAL_CHARACTERS + "]+$");
    }

    /**
     * 개인정보 포함시 True
     */
    private static boolean containsSensitiveInfo(String str, String birth, String tel) {
        if (birth == null || birth.trim().isEmpty() ||
                tel == null || tel.trim().isEmpty()) {
            return true; // null 또는 공백 문자열일 경우 true 반환
        }

        String sanitizedBirthDate = removeNonDigits(birth);
        String sanitizedTel = removeNonDigits(tel);
        if (sanitizedBirthDate.isEmpty() || sanitizedTel.isEmpty()) {
            return true; // 숫자가 전혀 없는 경우 true 반환
        }

        // 생년월일 매칭 검사
        if (str.contains(sanitizedBirthDate.substring(0, 2)) // 연도 확인
                || str.contains(sanitizedBirthDate.substring(2)) // 월일 확인
                || str.contains(sanitizedBirthDate) // 전체 확인
        ) {
            return true;
        }

        // 전화번호 매칭 검사
        if (str.contains(sanitizedTel)) {
            return true;
        }

        return false;
    }

    /**
     * 영문자, 숫자, 특수문자가 모두 1개 이상 포함되어있지 않으면 False
     */
    public static boolean containsRequiredTypes(String str) {
        return str.matches(".*[a-zA-Z].*") && // 영문자
                str.matches(".*[0-9].*") &&    // 숫자
                str.matches(".*[" + ALLOWED_SPECIAL_CHARACTERS + "].*"); // 특수문자
    }

    /**
     * 3개 이상의 동일한 숫자가 있다면 True
     */
    public static boolean hasRepeatedNumbers(String str) {
        return str.matches(".*(\\d)\\1{2,}.*");
    }

    /**
     * 연속 숫자 여부 확인
     *  - 연속 숫자가 3개 이상인 경우, True
     *      ex) 111, 123
     */
    public static boolean hasSequentialNumbers(String str) {
        for (int i = 0; i < str.length() - 2; i++) {
            char first = str.charAt(i);
            char second = str.charAt(i + 1);
            char third = str.charAt(i + 2);

            if (Character.isDigit(first) && Character.isDigit(second) && Character.isDigit(third)) {
                int diff1 = second - first;
                int diff2 = third - second;

                // 증가, 감소하는 연속 숫자 여부 확인
                if (diff1 == diff2 && Math.abs(diff1) == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 숫자를 제외한 문자 제거
     */
    private static String removeNonDigits(String input) {
        return input == null ? "" : input.replaceAll("[^0-9]", "");
    }
}
