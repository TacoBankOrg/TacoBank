package com.almagest_dev.tacobank_core_server.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisConnectionChecker implements ApplicationListener<ApplicationReadyEvent> {

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisConnectionChecker(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            redisConnectionFactory.getConnection().ping();
            log.info("Redis is connected!");
        } catch (Exception e) {
            log.error("Failed to connect to Redis: " + e.getMessage());
        }
    }
}
