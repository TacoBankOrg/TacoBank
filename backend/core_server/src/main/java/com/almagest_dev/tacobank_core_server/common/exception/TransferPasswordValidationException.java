package com.almagest_dev.tacobank_core_server.common.exception;

import org.springframework.http.HttpStatus;

public class TransferPasswordValidationException extends BaseCustomException {

    public TransferPasswordValidationException(String status, String message, HttpStatus httpStatus) {
        super(status, message, httpStatus);
    }

    public TransferPasswordValidationException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
