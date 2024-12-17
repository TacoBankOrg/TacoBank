package com.almagest_dev.tacobank_core_server.infrastructure.external.naver.dto.ocr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReceiptDto {
    private Meta meta; // 메타 정보
    private ReceiptResult result; // 영수증 OCR 인식 결과

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Meta {
        private String estimatedLanguage; // OCR 추정 언어
    }
}