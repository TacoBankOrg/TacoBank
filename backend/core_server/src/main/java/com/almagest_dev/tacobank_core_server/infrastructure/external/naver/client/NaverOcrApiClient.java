package com.almagest_dev.tacobank_core_server.infrastructure.external.naver.client;

import com.almagest_dev.tacobank_core_server.common.exception.OcrFailedException;
import com.almagest_dev.tacobank_core_server.infrastructure.external.naver.dto.ocr.NaverReceiptOcrResponseDto;
import com.almagest_dev.tacobank_core_server.infrastructure.external.naver.util.NaverApiUtil;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverOcrApiClient {
    @Value("${naver.ocr.api.url}")
    private String NAVER_OCR_API_URL;

    private final NaverApiUtil naverApiUtil;
    private final RestTemplate restTemplate;
    public NaverReceiptOcrResponseDto sendOcrReceipt(byte[] imageData, String imageName, String format, String jsonMessage) {
        log.info("NaverOcrApiClient::sendOcrReceipt START");
        // HttpHeaders 생성
        HttpHeaders headers = naverApiUtil.createOcrHeaders();
        log.info("NaverOcrApiClient::sendOcrReceipt Headers - {}", headers);

        // MultipartBodyBuilder를 사용해 요청 바디 생성
        MultipartBodyBuilder builder = new MultipartBodyBuilder();

        // message 추가
        builder.part("message", jsonMessage)
                .header("Content-Disposition", "form-data; name=message")
                .header("Content-Type", "application/json");

        // file 추가
        Resource imageResource = new ByteArrayResource(imageData) {
            @Override
            public String getFilename() {
                return imageName; // 전달받은 파일명 사용
            }
        };

        String validatedFormat = validateImageFormat(format);
        builder.part("file", imageResource)
                .header("Content-Disposition", "form-data; name=file; filename=" + imageName)
                .header("Content-Type", "image/" + validatedFormat);

        // HttpEntity 생성
        HttpEntity<?> requestEntity = new HttpEntity<>(builder.build(), headers);
        log.info("NaverOcrApiClient::sendOcrReceipt requestEntity - {}", requestEntity);

        // Naver OCR API 요청
        try {
            log.info("NaverOcrApiClient::sendOcrReceipt CALL Naver OCR API");
            String jsonResponse = restTemplate.postForObject(
                    NAVER_OCR_API_URL,
                    requestEntity,
                    String.class
            );
            log.info("NaverOcrApiClient::sendOcrReceipt Naver OCR API response - {}", jsonResponse);

            // DTO로 변환
            NaverReceiptOcrResponseDto response = mapJsonToDto(jsonResponse);

            // 응답 결과가 실패인 경우
            if (response == null || response.getImages() == null || response.getImages().isEmpty()) {
                throw new OcrFailedException("응답이 null이거나 유효하지 않습니다.");
            }

            log.info("NaverOcrApiClient::sendOcrReceipt END");
            return response;

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new OcrFailedException(ex.getMessage(), ex);

        }  catch (Exception ex) {
            ex.printStackTrace();
            throw new OcrFailedException("알 수 없는 오류 발생: " + ex.getMessage(), ex);
        }

    }

    public NaverReceiptOcrResponseDto mapJsonToDto(String json) {
        ObjectMapper objectMapper = new ObjectMapper()
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            return objectMapper.readValue(json, NaverReceiptOcrResponseDto.class);
        } catch (Exception e) {
            throw new RuntimeException("JSON 매핑 실패", e);
        }
    }

    // 파일 형식 지정
    private String validateImageFormat(String format) {
        List<String> supportedFormats = Arrays.asList("png", "jpg", "jpeg");
        if (!supportedFormats.contains(format.toLowerCase())) {
            throw new IllegalArgumentException("지원하지 않는 이미지 형식입니다: " + format);
        }
        return format.toLowerCase();
    }

}
