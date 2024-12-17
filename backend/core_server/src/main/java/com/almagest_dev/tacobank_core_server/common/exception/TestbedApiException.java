package com.almagest_dev.tacobank_core_server.common.exception;

public class TestbedApiException extends BaseCustomException {
    private String responseBody;
    public TestbedApiException(String message) {
        super(message);
    }

    public TestbedApiException(String responseBody, Throwable cause) {
        super(cause);
        this.responseBody = responseBody;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
