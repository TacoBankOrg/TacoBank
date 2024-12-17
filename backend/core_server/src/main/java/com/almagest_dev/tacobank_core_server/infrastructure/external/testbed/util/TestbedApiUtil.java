package com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class TestbedApiUtil {
    @Value("${testbed.api.key}")
    private String TESTBED_API_KEY;

    public HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + TESTBED_API_KEY);

        return headers;
    }
}
