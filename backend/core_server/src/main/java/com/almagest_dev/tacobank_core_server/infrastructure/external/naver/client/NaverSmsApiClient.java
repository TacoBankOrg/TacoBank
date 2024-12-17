package com.almagest_dev.tacobank_core_server.infrastructure.external.naver.client;

import com.almagest_dev.tacobank_core_server.common.exception.SmsSendFailedException;
import com.almagest_dev.tacobank_core_server.infrastructure.external.naver.dto.sms.NaverSmsRequestDto;
import com.almagest_dev.tacobank_core_server.infrastructure.external.naver.dto.sms.NaverSmsResponseDto;
import com.almagest_dev.tacobank_core_server.infrastructure.external.naver.util.NaverApiUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverSmsApiClient {

    @Value("${naver.sms.service.id}")
    private String NAVER_SMS_SERVICE_ID;
    private static final String NAVER_SMS_API_URL ="https://sens.apigw.ntruss.com/sms/v2";
    private final NaverApiUtil naverApiUtil;
    private final RestTemplate restTemplate;

    /**
     * Naver SMS API 요청
     *  - 메시지 리스트로 받아서 요청
     */
    public NaverSmsResponseDto sendSms(NaverSmsRequestDto requestBody, Long time) {
        log.info("NaverSmsApiClient::sendSms START");

        // Set Request Entity (+Header)
        HttpHeaders headers = naverApiUtil.createSmsHeaders(time);
        HttpEntity<NaverSmsRequestDto> requestEntity = new HttpEntity<>(requestBody, headers);
        log.info("NaverSmsApiClient::sendSms Request Header - " + headers + " | Body - " + requestBody);

        // Naver SMS API 요청
        try {
            log.info("NaverSmsApiClient::sendSms CALL Naver API");
            NaverSmsResponseDto response = restTemplate.postForObject(
                    NAVER_SMS_API_URL + "/services/" + NAVER_SMS_SERVICE_ID + "/messages",
                    requestEntity,
                    NaverSmsResponseDto.class
            );
            log.info("NaverSmsApiClient::sendSms Response: {}", response);

            if (response == null) {
                throw new SmsSendFailedException("응답이 null이거나 유효하지 않습니다.");
            }

            log.info("NaverSmsApiClient::sendSms END");
            return response;

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.error("NaverSmsApiClient::sendSms Http Exception: {}, Response Body: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new SmsSendFailedException("FAILURE", ex.getMessage(), HttpStatus.BAD_REQUEST,ex);
        }
    }

}
