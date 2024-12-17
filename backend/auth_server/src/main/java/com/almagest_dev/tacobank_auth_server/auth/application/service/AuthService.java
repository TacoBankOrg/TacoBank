package com.almagest_dev.tacobank_auth_server.auth.application.service;

import com.almagest_dev.tacobank_auth_server.auth.infrastructure.persistence.TokenBlackList;
import com.almagest_dev.tacobank_auth_server.auth.infrastructure.security.authentication.JwtProvider;
import com.almagest_dev.tacobank_auth_server.auth.presentation.dto.DuplicateEmailRequestDto;
import com.almagest_dev.tacobank_auth_server.auth.presentation.dto.SignupRequestDTO;
import com.almagest_dev.tacobank_auth_server.auth.domain.model.Member;
import com.almagest_dev.tacobank_auth_server.auth.domain.model.Role;
import com.almagest_dev.tacobank_auth_server.auth.domain.repository.MemberRepository;
import com.almagest_dev.tacobank_auth_server.auth.domain.repository.RoleRepository;
import com.almagest_dev.tacobank_auth_server.common.exception.InvalidTokenException;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final TokenBlackList tokenBlackList;
    private static final String ALLOWED_SPECIAL_CHARACTERS = "!@_";


    /**
     * 회원 가입 - Member 등록
     */
    public void registerMember(SignupRequestDTO requestDTO) {
        if (memberRepository.existsByEmailAndDeleted(requestDTO.getEmail(), "N")) {
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다.");
        }
        if (memberRepository.existsByTelAndDeletedNot(requestDTO.getTel(), "Y")) { // 삭제된 계정이 아닌데 동일 전화번호가 있는 경우
            throw new IllegalArgumentException("해당 전화번호로 이미 등록된 계정이 있습니다. 로그인 해주세요.");
        }

        // 비밀번호 유효성 검증
        String password = requestDTO.getPassword().trim(); // 공백 제거
        validatePassword(password, 8, requestDTO.getBirth(), requestDTO.getTel());

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(password);

        // Role 세팅
        Role role = roleRepository.findByRoleName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("시스템 오류가 발생했습니다. 관리자에게 문의하세요."));

        // 멤버 저장
        Member member = Member.createMember(requestDTO.getEmail(), encodedPassword, requestDTO.getName(), requestDTO.getBirth(), requestDTO.getTel(), role);
        memberRepository.save(member);
    }

    /**
     * 이메일 중복 검사
     */
    public void checkDuplicateEmail(DuplicateEmailRequestDto requestDto) {
        if (memberRepository.existsByEmailAndDeleted(requestDto.getEmail(), "N")) {
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다.");
        }
    }


    /**
     * 비밀번호 규칙 검사
     */
    public void validatePassword(String password, int minLen, String birth, String tel) {
        if (!isValidLength(password, minLen)) {
            throw new IllegalArgumentException("비밀번호는 최소 " + minLen + "자 이상이어야 합니다.");
        }
        if (!containsAllowedCharacters(password)) {
            throw new IllegalArgumentException("비밀번호는 허용되지 않은 문자를 포함할 수 없습니다.");
        }
        if (containsSensitiveInfo(password, birth, tel)) {
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
     * 길이 유효성 검사 (minLen 이상)
     */
    public static boolean isValidLength(String str, int minLen) {
        return str != null && str.length() >= minLen;
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

    /**
     * 세션 연장
     */
    public String extendSession(Cookie[] cookies) {
        // 쿠키에서 토큰 추출
        String token = getTokenFromCookies(cookies);
        if (token == null || !jwtProvider.validateToken(token)) {
            throw new InvalidTokenException("토큰이 유효하지 않습니다.");
        }

        // 블랙리스트 확인
        if (tokenBlackList.isTokenBlacklisted(token)) {
            throw new InvalidTokenException("블랙리스트에 등록된 토큰입니다.");
        }

        // 기존 토큰 블랙리스트에 추가
        long remainExpiration = jwtProvider.getRemainingExpiration(token);
        tokenBlackList.addTokenToBlackList(token, remainExpiration);

        // 새 토큰 발급
        long memberId = jwtProvider.getClaimsFromToken(token).get("memberId", Long.class);
        return jwtProvider.createToken(jwtProvider.getAuthentication(token), memberId);
    }

    /**
     * Cookie 에서 토큰 추출
     */
    private String getTokenFromCookies(Cookie[] cookies) {
        if (cookies == null) return null;

        for (Cookie cookie : cookies) {
            if ("Authorization".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}