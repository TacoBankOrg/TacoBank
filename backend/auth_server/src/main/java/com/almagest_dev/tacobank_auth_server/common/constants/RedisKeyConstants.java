package com.almagest_dev.tacobank_auth_server.common.constants;

public class RedisKeyConstants { // Redis Key 상수
    public static final String LOCK_PREFIX = "member:lock:";
    public static final String FAILURE_PREFIX = "login:failure:";
    public static final String BLACKLIST_PREFIX = "token:blacklist:";

    private RedisKeyConstants() {

    }
}
