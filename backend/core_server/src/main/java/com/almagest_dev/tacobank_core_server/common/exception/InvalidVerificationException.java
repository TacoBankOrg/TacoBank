package com.almagest_dev.tacobank_core_server.common.exception;

public class InvalidVerificationException extends BaseCustomException {

    public InvalidVerificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidVerificationException(String message) {
        super(message);
    }
}
