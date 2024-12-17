package com.almagest_dev.tacobank_core_server.common.exception;

import org.springframework.http.HttpStatus;

public class RedisSessionException extends BaseCustomException {

    public RedisSessionException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
