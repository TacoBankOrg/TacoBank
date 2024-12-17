package com.almagest_dev.tacobank_core_server.infrastructure.persistence;

import com.almagest_dev.tacobank_core_server.common.constants.RedisKeyConstants;
import com.almagest_dev.tacobank_core_server.common.utils.RedisSessionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenBlackList {
    private final RedisSessionUtil redisSessionUtil;

    /**
     * 토큰을 블랙리스트에 추가
     */
    public void addTokenToBlackList(String token, long remainExpiration) {
        String redisKey = RedisKeyConstants.BLACKLIST_PREFIX + token;

        // 블랙리스트에 토큰 저장 (TTL: 해당 토큰의 남은 만료 시간)
        redisSessionUtil.storeSessionData(redisKey, "BLACKLISTED", remainExpiration, TimeUnit.MILLISECONDS, false);

        log.info("TokenBlackList::addTokenToBlackList - 토큰 블랙리스트에 추가 (key: {}, ttl: {}ms)", redisKey, remainExpiration);
    }

    /**
     * 토큰이 블랙리스트에 있는지 확인
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    public boolean isTokenBlacklisted(String token) {
        log.info("TokenBlackList::isTokenBlackListed - {}", token);
        String redisKey = RedisKeyConstants.BLACKLIST_PREFIX + token;

        // Redis key 조회
        return redisSessionUtil.getValueIfExists(redisKey) != null;
    }
}
