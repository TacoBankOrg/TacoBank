package com.almagest_dev.tacobank_core_server.infrastructure.external.naver.dto.ocr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NaverReceiptOcrResponseDto {
    private String version;     // 버전 정보 (V2 만)
    private String requestId;   // API 호출 UUID
    private long timestamp;     // API 호출 시각(Timestamp) (1724976333747)
    private List<Image> images; // image 세부내용
}
