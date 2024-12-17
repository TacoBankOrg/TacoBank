package com.almagest_dev.tacobank_core_server.common.constants;

public class RedisKeyConstants { // Redis Key 상수
    public static final String SMS_LOCK_PREFIX = "sms:lock:";
    public static final String SMS_KEY_PREFIX = "sms:verification:";
    public static final String SMS_FAILURE_PREFIX = "sms:failures:";
    public static final String SMS_SUCCESS_PREFIX = "sms:success:";
    public static final String SMS_REQUEST_CNT_PREFIX = "sms:request:";
    public static final String PIN_FAILURE_PREFIX = "pin:failures:";
    public static final String BLACKLIST_PREFIX = "token:blacklist:";

    private RedisKeyConstants() {

    }
}
