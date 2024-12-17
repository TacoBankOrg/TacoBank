package com.almagest_dev.tacobank_core_server.common.exception;

import org.springframework.http.HttpStatus;

public class TransferException extends BaseCustomException {
    public TransferException(String status, String message, HttpStatus httpStatus) {
        super(status, message, httpStatus);
    }

    public TransferException(String message, HttpStatus httpStatus) {
        super(message, httpStatus);
    }
}
