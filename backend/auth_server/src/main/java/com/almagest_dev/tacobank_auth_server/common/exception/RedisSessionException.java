package com.almagest_dev.tacobank_auth_server.common.exception;

import org.springframework.http.HttpStatus;

public class RedisSessionException extends RuntimeException {
    private final HttpStatus httpStatus;

    public RedisSessionException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
