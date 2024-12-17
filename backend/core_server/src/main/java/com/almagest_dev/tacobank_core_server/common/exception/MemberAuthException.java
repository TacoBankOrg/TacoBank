package com.almagest_dev.tacobank_core_server.common.exception;

import org.springframework.http.HttpStatus;

public class MemberAuthException extends BaseCustomException {

    public MemberAuthException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
