package com.almagest_dev.tacobank_core_server.application.service;

import com.almagest_dev.tacobank_core_server.common.exception.InvalidVerificationException;
import com.almagest_dev.tacobank_core_server.domain.member.model.Member;
import com.almagest_dev.tacobank_core_server.domain.member.repository.MemberRepository;
import com.almagest_dev.tacobank_core_server.infrastructure.sms.util.SmsAuthUtil;
import com.almagest_dev.tacobank_core_server.presentation.dto.auth.SmsConfirmRequestDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.auth.SmsVerificationRequestDto;
import com.almagest_dev.tacobank_core_server.presentation.dto.auth.SmsVerificationResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final SmsAuthUtil smsAuthUtil;
    private final MemberRepository memberRepository;

    /**
     * 문자 인증 요청
     */
    public SmsVerificationResponseDto sendSmsVerificationCode(SmsVerificationRequestDto requestDto) {
        // member ID 가 요청에 있는 경우
        Long memberId = (requestDto.getMemberId() != null && requestDto.getMemberId() > 0) ? requestDto.getMemberId() : 0L;

        // Authentication이 있는 경우(= 인증 한 사용자)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // 인증 정보에서 멤버 ID 추출
            memberId = (Long) authentication.getDetails();
        }

        log.info("memberId: {} ", memberId);
        // member ID 가 요청에 있는 경우(= 로그인 한 사용자)
        if (memberId > 0) {
            // 사용자 정보 확인
            Member member = memberRepository.findByIdAndDeleted(memberId, "N")
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

            // 사용자 정보 확인된 경우, 기존 번호와 일치여부 확인 (전화번호 재설정인 경우 제외)
            if (!requestDto.getType().equals("tel")) {
                if (!member.getTel().equals(requestDto.getTel())) {
                    throw new IllegalArgumentException("요청한 전화번호가 회원정보에 등록된 전화번호와 일치하지 않습니다.");
                }
            } else { // 전화번호 재설정인 경우, 같은 번호라면 거부
                if (member.getTel().equals(requestDto.getTel())) {
                    throw new IllegalArgumentException("변경할 전화번호가 회원정보에 등록된 전화번호와 동일합니다.");
                }
            }
        }

        // 문자 인증번호 발송 및 인증 요청
        long logId = smsAuthUtil.sendVerificationCode(requestDto.getTel(), requestDto.getType(), memberId);
        return new SmsVerificationResponseDto(logId);
    }

    /**
     * 문자 인증 검증
     */
    public void confirmSmsVerificationCode(SmsConfirmRequestDto requestDto) {
        // member ID 가 요청에 있는 경우
        Long memberId = (requestDto.getMemberId() != null && requestDto.getMemberId() > 0) ? requestDto.getMemberId() : 0L;

        // Authentication이 있는 경우(= 인증 한 사용자)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            // 인증 정보에서 멤버 ID 추출
            memberId = (Long) authentication.getDetails();
        }

        // member ID 가 요청에 있는 경우(=로그인 한 사용자)
        if (memberId > 0) {
            // 사용자 정보 확인
            memberRepository.findByIdAndDeleted(memberId, "N")
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
            // 전화번호 일치여부는 요청시에만 수행
        }

        // 인증 여부 확인 & 인증번호 검증
        if (!smsAuthUtil.verifyCode(requestDto.getVerificationId(), requestDto.getTel(), requestDto.getInputCode(), requestDto.getType(), memberId)) {
            throw new InvalidVerificationException("인증 번호가 일치하지 않습니다.");
        }
    }
}
