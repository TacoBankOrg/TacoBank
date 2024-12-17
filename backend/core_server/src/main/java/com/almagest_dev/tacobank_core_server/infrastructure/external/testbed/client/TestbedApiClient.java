package com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.client;

import com.almagest_dev.tacobank_core_server.common.exception.TestbedApiException;
import com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.util.TestbedApiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestbedApiClient {
    private final String TESTBED_API_URL = "https://almagest.io/fintech/api";
    private final TestbedApiUtil testbedApiUtil;
    private final RestTemplate restTemplate;

    /**
     * Testbed API 요청
     */
    public <T, R> R  requestApi(T requestBody, String path, Class<R> responseType) {
        // Set Request Entity (+Header)
        HttpHeaders headers = testbedApiUtil.createHeaders();
        HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, headers);
        log.info("TestbedApiClient::requestApi Request Header - " + headers + " | Body - " + requestBody);

        try {
            R response = restTemplate.postForObject(
                    TESTBED_API_URL + path,
                    requestEntity,
                    responseType
            );

            if (response == null) {
                throw new TestbedApiException("API 응답이 없습니다.");
            }

            log.info("TestbedApiClient::requestApi Response - " + response);
            return response;

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.warn("TestbedApiClient::requestApi Testbed Server Error - {}", ex.getMessage());
            throw new TestbedApiException(ex.getResponseBodyAsString(), ex);
        }
    }

}
