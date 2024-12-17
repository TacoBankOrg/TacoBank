package com.almagest_dev.tacobank_core_server.common.exception;

import org.springframework.http.HttpStatus;

public abstract class BaseCustomException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String status;

    public BaseCustomException(String status, String message, HttpStatus httpStatus) {
        super(message);
        this.status = status;
        this.httpStatus = httpStatus;
    }

    public BaseCustomException(String status, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.httpStatus = httpStatus;
    }

    /**
     * 메시지만 설정하는 경우
     * (status, Http 상태 코드의 별도 지정이 필요하지 않는 경우)
     *  - status: FAILURE
     *  - Http Status Code: BAD_REQUEST
     */
    public BaseCustomException(String message, Throwable cause) {
        super(message, cause);
        this.status = "FAILURE";
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public BaseCustomException(String message) {
        super(message);
        this.status = "FAILURE";
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    /**
     * Message, HttpStatus만 받는 경우
     */
    public BaseCustomException(String message, HttpStatus httpStatus) {
        super(message);
        this.status = "FAILURE";
        this.httpStatus = httpStatus;
    }

    public BaseCustomException(Throwable cause) {
        super(cause);
        this.status = "FAILURE";
        this.httpStatus = HttpStatus.BAD_REQUEST;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getStatus() {
        return status;
    }
}