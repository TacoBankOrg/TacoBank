package com.almagest_dev.tacobank_core_server.infrastructure.sms.util;

import com.almagest_dev.tacobank_core_server.common.constants.RedisKeyConstants;
import com.almagest_dev.tacobank_core_server.common.exception.SmsSendFailedException;
import com.almagest_dev.tacobank_core_server.common.utils.JsonUtil;
import com.almagest_dev.tacobank_core_server.common.utils.RedisSessionUtil;
import com.almagest_dev.tacobank_core_server.domain.sms.model.SmsVerificationLog;
import com.almagest_dev.tacobank_core_server.domain.sms.repository.SmsVerificationLogRepository;
import com.almagest_dev.tacobank_core_server.infrastructure.external.naver.dto.sms.Message;
import com.almagest_dev.tacobank_core_server.infrastructure.external.naver.client.NaverSmsApiClient;
import com.almagest_dev.tacobank_core_server.infrastructure.external.naver.dto.sms.NaverSmsRequestDto;
import com.almagest_dev.tacobank_core_server.infrastructure.external.naver.dto.sms.NaverSmsResponseDto;
import com.almagest_dev.tacobank_core_server.infrastructure.sms.dto.VerificationDataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsAuthUtil {

    @Value("${naver.sms.from}")
    private String NAVER_SMS_FROM_NUM;
    // 허용된 인증 요청 타입 (회원가입: join | 비밀번호: pw | 출금 비밀번호: pin | 통합계좌연결: mydata | 휴대전화번호 수정: tel)
    private List<String> allowedRequestTypes = Arrays.asList("join", "pw", "pin", "mydata", "tel");

    private final NaverSmsApiClient naverSmsApiClient;
    private final RedisSessionUtil redisSessionUtil;
    private final JsonUtil jsonUtil;
    private final SmsVerificationLogRepository smsVerificationLogRepository;

    /**
     * Redis Key 생성
     */
    private String buildRedisKey(String prefix, String type, String tel) {
        return prefix + type + ":" + tel;
    }

    /**
     * 인증 번호 문자로 전송
     * @param memberId 문자 인증 성공시 다른 작업이 필요한 경우
     * @return long logId
     */
    public long sendVerificationCode(String tel, String requestType, Long memberId) {
        log.info("SmsAuthUtil::sendVerificationCode START");
        // 허용 가능한 요청 타입 검증
        if (!allowedRequestTypes.contains(requestType)) {
            log.warn("SmsAuthUtil::sendVerificationCode 허용되지 않은 요청 - tel: {}, requestType: {}", tel, requestType);
            throw new SmsSendFailedException("FAILURE", "허용되지 않은 요청입니다.", HttpStatus.BAD_REQUEST);
        }

        // 인증 제한(잠김) 여부 확인
        if (redisSessionUtil.isLocked(requestType + ":" + tel)) {
            throw new SmsSendFailedException("FAILURE", "인증이 차단되었습니다. 잠시 후 다시 시도하거나 고객센터로 문의해주세요.", HttpStatus.FORBIDDEN);
        }

        // 문자 인증 요청 - 지워지지 않은 세션, 성공한 세션이 있는지 확인
        String smsKey = buildRedisKey(RedisKeyConstants.SMS_KEY_PREFIX, requestType, tel); // SMS 문자 요청 세션 키
        String successKey = buildRedisKey(RedisKeyConstants.SMS_SUCCESS_PREFIX, requestType, tel); // SMS 문자 성공 세션 키
        // 기존 인증 요청 세션이 있는 경우
        if (redisSessionUtil.isKeyExists(smsKey)) {
            // 반복 요청에 대한 세션 관리
            String requestKey = buildRedisKey(RedisKeyConstants.SMS_REQUEST_CNT_PREFIX, requestType, tel);
            long requestCount = redisSessionUtil.incrementIfExists(requestKey, 1, 5, TimeUnit.MINUTES);
            log.warn("SmsAuthUtil::sendVerificationCode TTL 확인 반복 요청 - Key: {}, 요청 횟수: {}", smsKey, requestCount);
            if (requestCount > 5) { // 기존 요청이 있는데 5분안에 5회 초과 요청시 제한
                // 인증 및 계정 잠금 설정
                redisSessionUtil.lockAccess(requestType + ":" + tel, 10, TimeUnit.MINUTES);
                throw new SmsSendFailedException("FAILURE", "인증 요청 횟수가 초과하여 10분간 인증이 차단됩니다. 잠시 후 다시 시도하거나 고객센터로 문의해주세요.", HttpStatus.FORBIDDEN);
            }

            // 기존 세션 TTL이 1분 이상이면 제한
            long remainSeconds = redisSessionUtil.getTTLSeconds(smsKey);
            log.warn("SmsAuthUtil::sendVerificationCode TTL 확인 반복 요청 - Key: {}, 남은 TTL: {}", smsKey, remainSeconds);
            if (remainSeconds > 60) { // 1분 이상 남아있으면 제한
                throw new SmsSendFailedException("FAILURE", "기존 인증 요청이 진행 중 입니다. 1분 후에 다시 시도해주세요.", HttpStatus.BAD_REQUEST); // 보안상의 이유로 구체적인 초는 알려주지 않음
            } // 1분 이내인 경우 새로운 요청으로 덮어쓰기
        }
        if (redisSessionUtil.isKeyExists(successKey)) {
            throw new SmsSendFailedException("FAILURE", "이미 성공한 요청 입니다.", HttpStatus.BAD_REQUEST);
        }

        // 인증 번호 생성
        String code = generateCode();

        // Message 내용
        Message message = new Message();
        message.createMessage(tel, code);

        List<Message> messages = new ArrayList<>();
        messages.add(message);

        Long time = System.currentTimeMillis();
        // Set Request Body
        NaverSmsRequestDto requestBody = new NaverSmsRequestDto(
                "SMS"
                , "COMM"
                , "82"
                , NAVER_SMS_FROM_NUM
                , "기본 메시지 내용"
                , messages
        );

        // 문자 인증 요청 로그(SmsVerificationLog) INSERT
        SmsVerificationLog smsVerificationLog = SmsVerificationLog.createSmsVerificationLog(requestType, tel, code, jsonUtil.toJsonString(requestBody));
        smsVerificationLog = smsVerificationLogRepository.save(smsVerificationLog);
        Long logId = smsVerificationLog.getId();
        log.info("SmsAuthUtil::sendVerificationCode INSERT SmsVerificationLog - id: {}", logId);

        // 네이버 SMS API 호출
        log.info("SmsAuthUtil::sendVerificationCode CALL NaverSmsApiClient::sendSms");
        try {
            NaverSmsResponseDto response = naverSmsApiClient.sendSms(requestBody, time);

            // 네이버 SMS 응답 로그(SmsVerificationLog) UPDATE
            String requestId = response.getRequestId() != null ? response.getRequestId() : "";
            smsVerificationLog.updateResponseSmsVerificationLog(
                    requestId,
                    response.getRequestTime() != null ? LocalDateTime.parse(response.getRequestTime()) : null,
                    response.getStatusCode() != null ? response.getStatusCode() : "",
                    response.getStatusName() != null ? response.getStatusName() : "",
                    jsonUtil.toJsonString(response)
            );
            smsVerificationLogRepository.save(smsVerificationLog);
            log.info("SmsAuthUtil::sendVerificationCode UPDATE SmsVerificationLog - id: {}, requestId: {}", logId, requestId);

            // 응답 결과가 실패인 경우
            if ("fail".equals(response.getStatusName()) || !"202".equals(response.getStatusCode())) {
                log.warn("SmsAuthUtil::sendVerificationCode Naver Response Fail - {}", response);
                throw new SmsSendFailedException("인증번호 발송이 실패했습니다. 다시 시도해주세요.");
            }

            // Redis에 저장 (유효시간 3분)
            VerificationDataDto data = new VerificationDataDto(logId, requestId, code, memberId);

            log.info("SmsAuthUtil::storeVerificationData Redis Key - {}, Value - {}", smsKey, data);
            redisSessionUtil.storeSessionData(smsKey, data, 3, TimeUnit.MINUTES, false);

            // 응답 반환
            return logId;

        } catch (SmsSendFailedException ex) {
            // 해당 인증 요청 삭제
            redisSessionUtil.cleanupRedisKeys("SmsAuthUtil", smsKey);
            throw ex; // 예외 전달

        } finally {
            log.info("SmsAuthUtil::sendVerificationCode END");
        }
    }

    /**
     * 인증 번호 생성
     */
    public String generateCode() {
        return String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
    }

    /**
     * 인증 번호 검증
     */
    public boolean verifyCode(Long logId, String tel, String inputCode, String requestType, Long memberId) {
        log.info("SmsAuthUtil::verifyCode START");
        // 허용 가능한 요청 타입 검증
        if (!allowedRequestTypes.contains(requestType)) {
            log.warn("SmsAuthUtil::sendVerificationCode 허용되지 않은 요청 - tel: {}, requestType: {}", tel, requestType);
            throw new SmsSendFailedException("FAILURE", "허용되지 않은 요청입니다.", HttpStatus.BAD_REQUEST);
        }

        // 인증 제한(잠김) 여부 확인
        if (redisSessionUtil.isLocked(requestType + ":" + tel)) {
            throw new SmsSendFailedException("FAILURE", "인증이 차단되었습니다. 잠시 후 다시 시도하거나 고객센터로 문의해주세요.", HttpStatus.FORBIDDEN);
        }

        // 이미 성공한 건이 있는지 확인
        if (isVerificationSuccessful(tel, requestType, memberId)) {
            log.info("SmsAuthUtil::verifyCode SUCCESS - 인증 성공 내역 존재");
            return true;
        }

        // 세션 데이터 확인
        String smsKey = buildRedisKey(RedisKeyConstants.SMS_KEY_PREFIX, requestType, tel); // SMS 문자 요청 세션 키
        VerificationDataDto verificationData;
        verificationData = redisSessionUtil.getSessionData(smsKey, VerificationDataDto.class, false);
        if (verificationData == null) {
            throw new SmsSendFailedException("인증 요청 내역이 없습니다.");
        }

        log.info("SmsAuthUtil::storeVerificationData Redis Key - {}, Value - {}", smsKey, verificationData);

        // verificationData null check
        if (verificationData.getLogId() == null || verificationData.getVerificationCode() == null) {
            log.warn("SmsAuthUtil::verifyCode 세션 Data가 없음 - verificationData: {}" ,verificationData);
            throw new SmsSendFailedException("세션이 유효하지 않습니다.");
        }
        // 문자 인증 요청 식별 ID(logId) 일치 여부 확인
        if (!logId.equals(verificationData.getLogId())) {
            log.warn("SmsAuthUtil::verifyCode 문자 인증 요청 ID가 상이함 - Request logId: {}, verificationData logId: {}" ,logId, verificationData.getLogId());
            throw new SmsSendFailedException("요청한 유효 내역이 없습니다.");
        }

        // 인증 코드 확인
        String storedCode = verificationData.getVerificationCode();
        log.info("SmsAuthUtil::verifyCode smsKey - {} inputCode - {} storedCode - {}", smsKey, inputCode, storedCode);

        // 비밀번호 검증
        String pinFailureKey = buildRedisKey(RedisKeyConstants.SMS_FAILURE_PREFIX, requestType, tel); // 비밀번호 검증 실패 횟수 세션 키
        if (inputCode.equals(storedCode)) { // 성공
            log.info("SmsAuthUtil::verifyCode VERIFIED SUCCESS");

            // SmsVerificationLog 인증 상태 UPDATE
            updateSmsVerificationStatus("VERIFIED", tel, inputCode, verificationData.getLogId(), verificationData);

            // Redis 세션 삭제 - 인증 요청, 실패 횟수 세션
            redisSessionUtil.cleanupRedisKeys("SmsAuthUtil", pinFailureKey);
            redisSessionUtil.cleanupRedisKeys("SmsAuthUtil", smsKey);

            // Redis 성공 세션 생성
            String successKey = buildRedisKey(RedisKeyConstants.SMS_SUCCESS_PREFIX, requestType, tel); // SMS 문자 성공 세션 키
            redisSessionUtil.storeSessionData(successKey, verificationData, 3, TimeUnit.MINUTES, false);
            return true;

        } else {
            // 실패 횟수 증가 및 TTL 설정
            Long failCnt = redisSessionUtil.incrementIfExists(pinFailureKey, 1L, 3, TimeUnit.MINUTES);
            log.info("SmsAuthUtil - [{}] verifyCode 인증번호 불일치 {}번째", pinFailureKey, failCnt);

            // 실패 횟수 초과 시 인증 실패로 종료 & 10분간 인증 불가 및 계정 잠금
            if (failCnt >= 5) {
                // SmsVerificationLog 인증 상태 UPDATE
                updateSmsVerificationStatus("FAILURE", tel, inputCode, verificationData.getLogId(), verificationData);

                // 인증 및 계정 잠금 설정
                redisSessionUtil.lockAccess(requestType + ":" + tel, 10, TimeUnit.MINUTES);
                throw new SmsSendFailedException("FAILURE", "인증 번호 입력 횟수가 초과하여 10분간 인증이 차단됩니다. 잠시 후 다시 시도하거나 고객센터로 문의해주세요.", HttpStatus.FORBIDDEN);
            }

            // 실패 메시지 반환
            throw new SmsSendFailedException("FAILURE", "인증번호가 올바르지 않습니다. 남은 시도 횟수: " + (5 - failCnt), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * 인증 성공 확인
     */
    public boolean isVerificationSuccessful(String tel, String requestType, Long memberId) {
        log.info("SmsAuthUtil::isVerificationSuccessful - tel: {}, requestType: {}", tel, requestType);

        // 성공한 내역이 있는지 확인
        String successKey = buildRedisKey(RedisKeyConstants.SMS_SUCCESS_PREFIX, requestType, tel); // SMS 문자 성공 세션 키
        VerificationDataDto verificationData = redisSessionUtil.getSessionData(successKey, VerificationDataDto.class, false);
        if (verificationData == null) {
            log.warn("SmsAuthUtil::isVerificationSuccessful 인증 성공 내역이 없습니다.");
            return false;
        }

        // 성공 세션 데이터에 memberId가 있는 경우, 이후 들어온 요청의 MemberID와 같은지 확인
        if (verificationData.getMemberId() != null && verificationData.getMemberId() > 0) {
            if (verificationData.getMemberId() != memberId) {
                log.warn("SmsAuthUtil::isVerificationSuccessful 멤버 정보 불일치 - verificationData memberId: {}, request memberId: {}", verificationData.getMemberId(), memberId);
                return false;
            }
        }
        return true;
    }

    /**
     * SMS 관련 세션 삭제 - 성공 세션 제외
     */
    public void cleanupAllSmsSession(String tel, String requestType) {
        redisSessionUtil.cleanupRedisKeys("SmsAuthUtil", tel,
                buildRedisKey(RedisKeyConstants.SMS_KEY_PREFIX, requestType, tel)
                , buildRedisKey(RedisKeyConstants.SMS_FAILURE_PREFIX, requestType, tel)
        );
    }

    /**
     * SMS 성공 세션 삭제
     */
    public void cleanupSuccessSmsSession(String tel, String requestType) {
        redisSessionUtil.cleanupRedisKeys("SmsAuthUtil", buildRedisKey(RedisKeyConstants.SMS_SUCCESS_PREFIX, requestType, tel));
    }

    /**
     * SmsVerificationLog 인증 상태 UPDATE
     */
    private SmsVerificationLog updateSmsVerificationStatus(String status, String tel, String inputCode, Long logId, VerificationDataDto verificationData) {
        SmsVerificationLog smsVerificationLog = smsVerificationLogRepository.findById(logId).orElse(null);

        if (smsVerificationLog == null) {
            // 비즈니스 로직은 수행할 수 있게 예외처리 하지 않고, 로그만 남김
            log.warn("SmsAuthUtil::verifyCode SmsVerificationLog 인증 상태 UPDATE 실패 - 조회된 Log 데이터 없음 (tel: {}, inputCode: {}, verificationData: {})", tel, inputCode, verificationData);
        } else {
            smsVerificationLog.updateVerificationStatusAndInputCode(status, inputCode);
            smsVerificationLogRepository.save(smsVerificationLog);
        }
        return smsVerificationLog;
    }
}
