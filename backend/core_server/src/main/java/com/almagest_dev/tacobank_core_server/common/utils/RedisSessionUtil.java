package com.almagest_dev.tacobank_core_server.common.utils;

import com.almagest_dev.tacobank_core_server.common.constants.RedisKeyConstants;
import com.almagest_dev.tacobank_core_server.common.exception.RedisSessionException;
import com.almagest_dev.tacobank_core_server.infrastructure.encryption.EncryptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSessionUtil {
    private final EncryptionUtil encryptionUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 세션 ID 생성 메서드 (암호화)
     */
    public String generateEncryptSessionId(Long memberId, String uniqueKey) {
        if (memberId == null || uniqueKey == null || uniqueKey.isEmpty()) {
            throw new RedisSessionException("요청정보가 유효하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
        String input = memberId + ":" + uniqueKey;
        try {
            return encryptionUtil.encrypt(input);
        } catch (Exception e) {
            throw new RedisSessionException("Session ID 생성 실패", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Redis 저장
     */
    public <T> void storeSessionData(String redisKey, T data, long duration, TimeUnit unit, boolean encryptFlag) {
        if (redisKey == null || data == null) {
            throw new RedisSessionException("Key 또는 데이터가 없습니다.", HttpStatus.BAD_REQUEST);
        }

        try {
            String valueToStore;

            if (data instanceof String) {
                // 데이터가 String인 경우 그대로 저장
                valueToStore = (String) data;
            } else {
                // String이 아닌 경우 ObjectMapper를 사용하여 JSON 직렬화
                valueToStore = objectMapper.writeValueAsString(data);
            }

            if (encryptFlag) { // 암호화 해야하는 경우
                valueToStore = encryptionUtil.encrypt(valueToStore);
            }
            // Redis에 Set
            redisTemplate.opsForValue().set(redisKey, valueToStore, duration, unit);
        } catch (Exception e) {
            String errorMessage = (e instanceof IllegalArgumentException)
                    ? "Redis Key 또는 데이터 직렬화 오류"
                    : "Redis 저장 중 암호화 오류";
            throw new RedisSessionException(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Redis 조회
     */
    public <T> T getSessionData(String redisKey, Class<T> clazz, boolean encryptFlag) {
        if (StringUtils.isBlank(redisKey)) {
            throw new RedisSessionException("유효하지 않은 요청 입니다.", HttpStatus.BAD_REQUEST);
        }

        // Redis 세션 조회
        String sessionData = redisTemplate.opsForValue().get(redisKey);
        if (sessionData == null) {
            log.info("RedisSessionUtil::getSessionData 요청 내역이 존재하지 않습니다 - key: {}", redisKey);
            return null;
        }

        try {
            if (encryptFlag) {
                sessionData = encryptionUtil.decrypt(sessionData);
            }
            return objectMapper.readValue(sessionData, clazz);
        } catch (Exception e) {
            String message = (e.getMessage() == null) ? "Redis 조회 중 오류" : e.getMessage();
            throw new RedisSessionException(message, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Redis 조회 - 평문 Value
     */
    public String getRawSessionData(String redisKey) {
        if (StringUtils.isBlank(redisKey)) {
            throw new RedisSessionException("유효하지 않은 요청 입니다.", HttpStatus.BAD_REQUEST);
        }

        String value = redisTemplate.opsForValue().get(redisKey);
        if (value == null) {
            throw new RedisSessionException("세션이 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        return value;
    }

    /**
     * Redis 조회 - Redis Hash (String)
     */
    public <K, V> Map<K, V> getHashSessionData(String redisKey, Class<K> keyClass, Class<V> valueClass) {
        if (StringUtils.isBlank(redisKey)) {
            throw new RedisSessionException("유효하지 않은 요청 입니다.", HttpStatus.BAD_REQUEST);
        }

        // Redis에서 Hash 데이터를 가져옵니다.
        Map<Object, Object> rawData = redisTemplate.opsForHash().entries(redisKey);
        if (rawData == null || rawData.isEmpty()) {
            throw new RedisSessionException("세션이 존재하지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        // 데이터 변환
        try {
            return rawData.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> objectMapper.convertValue(entry.getKey(), keyClass),   // Key 변환
                            entry -> objectMapper.convertValue(entry.getValue(), valueClass) // Value 변환
                    ));
        } catch (Exception e) {
            throw new RedisSessionException("Redis 데이터 변환 중 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Redis 키 존재 여부 확인 후 값 반환
     * @return 키가 존재하면 값 반환, 존재하지 않으면 null 반환
     */
    public String getValueIfExists(String redisKey) {
        if (StringUtils.isBlank(redisKey)) {
            throw new RedisSessionException("유효하지 않은 요청입니다.", HttpStatus.BAD_REQUEST);
        }

        try {
            String value = redisTemplate.opsForValue().get(redisKey);
            return value;
        } catch (Exception e) {
            log.warn("SessionUtil::getValueIfExists Redis 키 확인 및 값 조회 중 예외 발생 - Key: {}, Error: {}", redisKey, e.getMessage());
            throw new RedisSessionException("Redis 키 확인 또는 값 조회 중 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Redis 업데이트
     *  - updateTtlFlag = true: 만료시간 갱신
     *  - updateTtlFlag = false: 기존 TTL 유지
     */
    public <T> void updateSessionData(String redisKey, T data, long duration, TimeUnit unit, boolean updateTtlFlag, boolean encryptFlag) {
        try {
            String jsonData = objectMapper.writeValueAsString(data);
            if (encryptFlag) jsonData = encryptionUtil.encrypt(jsonData); // 암호화 여부에 따라 암호화

            if (updateTtlFlag) { // TTL 갱신하는 경우
                redisTemplate.opsForValue().set(redisKey, jsonData, duration, unit);

            } else {
                // 현재 TTL 가져오기
                Long currentTtl = redisTemplate.getExpire(redisKey, TimeUnit.MILLISECONDS);
                if (currentTtl == null || currentTtl <= 0) {
                    throw new RedisSessionException("Redis 키의 TTL을 가져올 수 없거나 키가 만료되었습니다.", HttpStatus.BAD_REQUEST);
                }

                // 키와 데이터를 설정하되 TTL은 유지
                redisTemplate.opsForValue().set(redisKey, jsonData, currentTtl, TimeUnit.MILLISECONDS);
            }

        } catch (Exception e) {
            throw new RedisSessionException("Redis 업데이트 중 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Redis 키 삭제
     * @param className 메서드 호출 클래스 명
     * @param keys 삭제할 1개 이상의 키
     */
    public void cleanupRedisKeys(String className, String... keys) {
        // Redis 키 삭제 로직
        for (String key : keys) {
            try {
                boolean deleted = Boolean.TRUE.equals(redisTemplate.delete(key));
                log.info("{} - Redis 키 삭제 - Key: {}, 성공 여부: {}", className, key, deleted);
            } catch (Exception redisEx) {
                log.warn("{} - Redis 키 삭제 중 예외 발생 - Key: {}, Error: {}", className, key, redisEx.getMessage());
            }
        }
    }

    /**
     * Redis 키의 값을 증가시키고 만료 시간을 설정
     *
     * @param redisKey Redis 키
     * @param incrementValue 증가할 값 (기본값 1L)
     * @param ttl 만료 시간 (null인 경우 기존 TTL 유지)
     * @param timeUnit TTL의 단위 (ttl이 null이면 무시됨)
     * @return 증가된 값
     */
    public Long incrementIfExists(String redisKey, long incrementValue, long ttl, TimeUnit timeUnit) {
        try {
            // 값 증가 - 값이 없을 경우, 1로 초기화
            Long newValue = redisTemplate.opsForValue().increment(redisKey, incrementValue);

            // TTL 설정은 키가 처음 생성된 경우에만 수행
            setTTLIfAbsent(redisKey, ttl, timeUnit);

            return newValue;
        } catch (Exception e) {
            throw new RedisSessionException("Redis 증분 또는 TTL 설정 중 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Redis Key 존재 여부 확인
     */
    public boolean isKeyExists(String redisKey) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
    }

    /**
     * Redis TTL 조회
     * @return 남은 TTL (초 단위), 존재하지 않으면 -1 반환
     */
    public long getTTLSeconds(String redisKey) {
        Long ttl = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        if (ttl == null || ttl == -1) {
            log.warn("RedisSessionUtil::getTTL Redis TTL 조회 NULL - Key: {}", redisKey);
            throw new RedisSessionException("요청 내역이 존재하지 않습니다", HttpStatus.BAD_REQUEST);
        }
        return ttl;
    }

    /**
     * 접속(계정, 인증) 잠금
     * @param sessionId prefix 를 제외한 세션 키
     */
    public void lockAccess(String sessionId, long duration, TimeUnit unit) {
        String lockKey = RedisKeyConstants.SMS_LOCK_PREFIX + sessionId;
        redisTemplate.opsForValue().set(lockKey, "LOCKED", duration, unit);
        log.info("RedisSessionUtil::lockAccount - 접속 잠금 완료 (sessionId: {})", sessionId);

        // sessionId로 세팅된 이외 세션은 모두 삭제
        cleanupRedisKeys("RedisSessionUtil::lockAccount"
                , RedisKeyConstants.SMS_KEY_PREFIX + sessionId
                , RedisKeyConstants.SMS_FAILURE_PREFIX + sessionId
                , RedisKeyConstants.SMS_SUCCESS_PREFIX + sessionId
                , RedisKeyConstants.SMS_REQUEST_CNT_PREFIX + sessionId
        );
    }

    /**
     * 계정 or 인증 잠금 확인
     * @param sessionId Prefix 뒤에 붙는 세션 고유값
     * @return boolean 잠긴 경우: true | 잠기지 않은 경우: false
     */
    public boolean isLocked(String sessionId) {
        // 인증 잠금 여부 확인
        String lockStatus = getValueIfExists(RedisKeyConstants.SMS_LOCK_PREFIX + sessionId);
        if ("LOCKED".equals(lockStatus)) {
            log.info("RedisSessionUtil::isLocked - 접속 잠금 상태 (sessionId: {})", sessionId);
            return true;
        }
        return false;
    }


    /**
     * TTL, TimeUnit 있는 경우, TTL 설정
     */
    private void setTTLIfAbsent(String redisKey, long ttl, TimeUnit timeUnit) {
        if (redisTemplate.getExpire(redisKey) == -1) { // -1은 만료시간이 설정되지 않았음을 의미
            // TTL이 없는 경우 세션 생성 불가 (무기한 세션은 허용 안함)
            if (ttl <= 0 || timeUnit == null) {
                throw new RedisSessionException("Redis TTL 설정 값 오류 - TTL: " + ttl + ", TimeUnit: " + timeUnit, HttpStatus.INTERNAL_SERVER_ERROR);
            }
            redisTemplate.expire(redisKey, ttl, timeUnit);
        }
    }
}
