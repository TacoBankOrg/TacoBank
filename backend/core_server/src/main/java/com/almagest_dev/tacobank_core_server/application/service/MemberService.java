package com.almagest_dev.tacobank_core_server.application.service;

import com.almagest_dev.tacobank_core_server.common.constants.RedisKeyConstants;
import com.almagest_dev.tacobank_core_server.common.exception.InvalidVerificationException;
import com.almagest_dev.tacobank_core_server.common.exception.SmsSendFailedException;
import com.almagest_dev.tacobank_core_server.common.utils.MaskingUtil;
import com.almagest_dev.tacobank_core_server.common.utils.RedisSessionUtil;
import com.almagest_dev.tacobank_core_server.common.utils.ValidationUtil;
import com.almagest_dev.tacobank_core_server.domain.friend.model.Friend;
import com.almagest_dev.tacobank_core_server.domain.friend.repository.FriendRepository;
import com.almagest_dev.tacobank_core_server.domain.member.model.Member;
import com.almagest_dev.tacobank_core_server.domain.member.repository.MemberRepository;
import com.almagest_dev.tacobank_core_server.infrastructure.sms.util.SmsAuthUtil;
import com.almagest_dev.tacobank_core_server.presentation.dto.member.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final SmsAuthUtil smsAuthUtil;
    private final RedisSessionUtil redisSessionUtil;
    private final PasswordEncoder passwordEncoder;
    private final FriendRepository friendRepository;

    /**
     * ID로 Member 조회
     */
    public MemberResponseDto findMemberById(Long id) {
        if (id == null || id < 1) {
            throw new IllegalArgumentException("회원 정보가 없습니다.");
        }
        Member member = memberRepository.findByIdAndDeleted(id, "N")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        return new MemberResponseDto(
                member.getId(),
                member.getEmail(),
                member.getName(),
                member.getTel(),
                member.getBirth(),
                member.getUserFinanceId()
        );
    }

    /**
     * 전화번호로 멤버 이메일 조회
     */
    public FindEmailResponseDto findEmailByTel(FindEmailRequestDto requestDto) {
        Member member = memberRepository.findByTel(requestDto.getTel())
                .orElseThrow(() -> new IllegalArgumentException("등록된 이메일을 찾을 수 없습니다."));

        return new FindEmailResponseDto(MaskingUtil.maskEmail(member.getEmail()));
    }

    /**
     * 회원정보 수정
     */
    public void updateMemberTel(Long memberId, UpdateMemberRequestDto requestDto) {
        if (memberId == null || memberId < 1) {
            throw new IllegalArgumentException("회원 정보가 없습니다.");
        }
        if (StringUtils.isBlank(requestDto.getTel())) {
            throw new IllegalArgumentException("변경할 전화번호가 없습니다.");
        }
        if (!ValidationUtil.isValidLength(requestDto.getTel(), 10, 15)) {
            throw new IllegalArgumentException("전화번호는 10자 이상 16자 이하이어야 합니다.");
        }

        // 회원 정보 확인
        Member member = memberRepository.findByIdAndDeleted(memberId, "N")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));
        // 문자 인증 성공 세션 확인
        if (!smsAuthUtil.isVerificationSuccessful(requestDto.getTel(), "tel", memberId)) {
            throw new IllegalArgumentException("본인 인증 성공 내역이 없습니다. 본인 인증이 완료되어야 전화번호 수정이 가능합니다.");
        }

        member.changeTel(requestDto.getTel());
        memberRepository.save(member);

        // 수정 성공시 성공 세션 삭제
        smsAuthUtil.cleanupSuccessSmsSession(requestDto.getTel(), "tel");
    }

    /**
     * 비밀번호 찾기 1) 본인 인증 & 인증번호 발송
     */
    public FindPasswordResponseDto findPasswordAndSendSmsAuth(FindPasswordRequestDto requestDto) {
        // 1. 멤버 확인
        Member member = memberRepository.findByEmailAndTel(requestDto.getEmail(), requestDto.getTel())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보를 찾을 수 없습니다."));

        // 2. 문자 인증번호 발송 및 인증 요청
        long logId = smsAuthUtil.sendVerificationCode(member.getTel(), "pw", member.getId());

        return new FindPasswordResponseDto(member.getId(), logId);
    }

    /**
     * 비밀번호 찾기 : 새로운 비밀번호로 설정(본인 인증 후)
     */
    public void confirmPassword(ConfirmPasswordRequestDto requestDto) {
        // 멤버 확인
        Member member = memberRepository.findByIdAndDeleted(requestDto.getMemberId(), "N")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 문자 인증 성공 세션 확인
        if (!smsAuthUtil.isVerificationSuccessful(member.getTel(), "pw", member.getId())) {
            throw new IllegalArgumentException("본인 인증 성공 내역이 없습니다.");
        }

        // 비밀번호 규칙 검사 & Member UPDATE
        validateAndUpdatePassword(member, requestDto.getNewPassword(), requestDto.getConfirmPassword());

        // 성공시 성공 세션 삭제
        smsAuthUtil.cleanupSuccessSmsSession(member.getTel(), "pw");
    }

    /**
     * 비밀번호 재설정: 기존 비밀번호, 새 비밀번호
     */
    public void resetMemberPassword(ResetPasswordRequestDto requestDto) {
        // 멤버 확인
        Member member = memberRepository.findByIdAndDeleted(requestDto.getMemberId(), "N")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 기존 비밀번호 확인
        if (!passwordEncoder.matches(requestDto.getCurrentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호 규칙 검사 & Member UPDATE
        validateAndUpdatePassword(member, requestDto.getNewPassword(), requestDto.getConfirmPassword());
    }

    /**
     * 비밀번호 규칙 검사 & UPDATE
     */
    private void validateAndUpdatePassword(Member member, String newPassword, String confirmPassword) {
        // 비밀번호 공백 제거
        newPassword = newPassword.trim();
        confirmPassword = confirmPassword.trim();

        // 비밀번호 규칙 검사
        ValidationUtil.validatePassword(newPassword, 8, member.getBirth(), member.getTel());

        // 새 비밀번호, 확인 비밀번호 일치 여부
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호 Update & 저장
        String encodedPassword = passwordEncoder.encode(newPassword);
        member.changePassword(encodedPassword);

        memberRepository.save(member);
    }

    /**
     * 회원 탈퇴
     */
    public void deactivateMember(Long id) {
        if (id == null || id < 1) {
            throw new IllegalArgumentException("회원 정보가 없습니다.");
        }
        Member member = memberRepository.findByIdAndDeleted(id, "N")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않거나 이미 탈퇴한 회원입니다."));

        member.deactivate(); // 회원상태 비활성화
        memberRepository.save(member);
    }

    /**
     * 출금 비밀번호 설정/저장 - 본인 인증 후 설정
     */
    public void createTransferPin(CreatePinRequestDto requestDto) {
        log.info("MemberService::setTransferPin START");

        // Member 조회
        Member member = memberRepository.findByIdAndDeleted(requestDto.getMemberId(), "N")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 문자 인증 성공 세션 확인
        if (!smsAuthUtil.isVerificationSuccessful(member.getTel(), "pin", member.getId())) {
            throw new IllegalArgumentException("본인 인증 성공 내역이 없습니다.");
        }

        // 출금 비밀번호 유효성 검사
        ValidationUtil.validateTransferPin(requestDto.getTransferPin());

        // 출금 비밀번호, 확인 비밀번호 일치 여부
        if (!requestDto.getConfirmTransferPin().equals(requestDto.getConfirmTransferPin())) {
            throw new IllegalArgumentException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호 해싱 및 저장
        String encodedPin = passwordEncoder.encode(requestDto.getTransferPin());
        member.changeTransferPin(encodedPin);
        memberRepository.save(member);

        // 성공시 성공 세션 삭제
        smsAuthUtil.cleanupSuccessSmsSession(member.getTel(), "pin");

        log.info("MemberService::setTransferPin END - Transfer PIN 설정 완료");
    }

    /**
     * 현재 출금 비밀번호 확인
     */
    public void validateTransferPin(ValidatePinRequestDto requestDto) {
        log.info("MemberService::validateTransferPin START");

        // Member 조회
        Member member = memberRepository.findByIdAndDeleted(requestDto.getMemberId(), "N")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 기존 비밀번호 설정 여부 확인
        if (member.getTransferPin() == null) {
            throw new IllegalArgumentException("출금 비밀번호를 설정하지 않았습니다. 본인 인증 후 출금 비밀번호를 설정해주세요.");
        }

        // 출금 비밀번호 확인
        if (!passwordEncoder.matches(requestDto.getTransferPin(), member.getTransferPin())) {
            // 실패 횟수 증가 및 TTL 설정
            Long failCnt = redisSessionUtil.incrementIfExists(RedisKeyConstants.PIN_FAILURE_PREFIX + member.getId(), 1L, 5, TimeUnit.MINUTES);
            log.info("MemberService::validateTransferPin - [{}] 출금 비밀번  불일치 {}번째", RedisKeyConstants.PIN_FAILURE_PREFIX + member.getId(), failCnt);
            // 5회 이상 실패하면 초기화 & 본인인증 & 재설정 필요
            if (failCnt >= 5) {
                member.changeTransferPin(null);
                memberRepository.save(member);
                throw new SmsSendFailedException("FAILURE", "인증 번호 입력 횟수가 초과하여 출금 비밀번호가 초기화 됩니다. 다시 설정해주세요.", HttpStatus.FORBIDDEN);
            }

            throw new InvalidVerificationException("출금 비밀번호가 일치하지 않습니다. 남은 시도 횟수: " + (5 - failCnt));
        }

        log.info("MemberService::validateTransferPin END");
    }

    /**
     * 출금 비밀번호 수정
     *  - 로그인 한 상태에서만 수정이 가능함
     */
    public void resetTransferPin(ChangePinRequestDto requestDto) {
        log.info("MemberService::resetTransferPin START");
        // Member 조회
        Member member = memberRepository.findByIdAndDeleted(requestDto.getMemberId(), "N")
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // 기존 비밀번호 설정 여부 확인
        if (member.getTransferPin() == null) {
            throw new IllegalArgumentException("출금 비밀번호를 설정하지 않았습니다. 본인 인증 후 출금 비밀번호를 설정해주세요.");
        }

        // 기존 비밀번호 확인
        if (!passwordEncoder.matches(requestDto.getCurrentPin(), member.getTransferPin())) {
            throw new InvalidVerificationException("출금 비밀번호가 일치하지 않습니다.");
        }

        // 새로운 비밀번호 유효성 검사
        ValidationUtil.validateTransferPin(requestDto.getNewPin());

        // 새로운 비밀번호와 확인 비밀번호 일치여부
        if (!requestDto.getNewPin().equals(requestDto.getConfirmPin())) {
            throw new InvalidVerificationException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        // 새로운 비밀번호 해싱 및 저장
        String encodedPin = passwordEncoder.encode(requestDto.getNewPin());
        member.changeTransferPin(encodedPin);
        memberRepository.save(member);

        log.info("MemberService::resetTransferPin END");
    }


    /**
     * 이메일로 친구 검색
     */
    public MemberSearchResponseDto searchMemberByEmail(String email, Long memberId) {
        Member member = memberRepository.findByEmail(email)
                .filter(m -> "N".equals(m.getDeleted()))
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 회원을 찾을 수 없습니다."));

        if (member.getId().equals(memberId)) {
            throw new IllegalArgumentException("자신은 검색할 수 없습니다.");
        }

        boolean isBlocked = friendRepository.findByRequesterIdAndReceiverId(memberId, member.getId())
                .map(Friend::getStatus)
                .filter(status -> "BLOCKED".equals(status) || "BLOCKED_BY".equals(status))
                .isPresent();

        if (isBlocked) {
            throw new IllegalArgumentException("해당 회원은 차단 상태이므로 검색할 수 없습니다.");
        }

        boolean isDeleted = friendRepository.findByRequesterIdAndReceiverId(memberId, member.getId())
                .map(Friend::getStatus)
                .filter("DEL"::equals)
                .isPresent();

        if (isDeleted) {
            throw new IllegalArgumentException("해당 회원은 삭제 상태이므로 검색할 수 없습니다.");
        }

        // 친구 상태 조회
        String friendStatus = friendRepository.findByRequesterIdAndReceiverId(memberId, member.getId())
                .map(Friend::getStatus)
                .orElse("NONE"); // 상태가 없으면 "NONE" 반환

        // Member 데이터를 MemberSearchResponseDto로 변환하여 반환
        return new MemberSearchResponseDto(
                member.getId(),
                member.getName(),
                member.getEmail(),
                friendStatus // 친구 상태 추가
        );
    }
}
