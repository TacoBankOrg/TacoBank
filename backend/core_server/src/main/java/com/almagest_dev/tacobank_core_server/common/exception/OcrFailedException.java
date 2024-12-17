package com.almagest_dev.tacobank_core_server.common.exception;

public class OcrFailedException extends BaseCustomException {
    public OcrFailedException(String message) {
        super(message);
    }
    public OcrFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
