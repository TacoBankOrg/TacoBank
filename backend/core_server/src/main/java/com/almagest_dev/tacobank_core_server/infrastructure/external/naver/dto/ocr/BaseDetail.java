package com.almagest_dev.tacobank_core_server.infrastructure.external.naver.dto.ocr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BaseDetail {
    private String text; // 인식된 텍스트
    private Formatted formatted; // 인식된 텍스트 정보
    private String keyText; // 텍스트 키 값
    private Float confidenceScore; // 신뢰도
    private List<BoundingPoly> boundingPolys; // Bounding Poly 세부 정보
    private List<MaskingPoly> maskingPolys; // Masking Poly 세부 정보

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Formatted {
        private String value;   // 인식된 텍스트 값
        private String year;    // 인식된 날짜의 연도(yyyy)
        private String month;   // 인식된 날짜의 월(MM)
        private String day;     // 인식된 날짜의 일(dd)
        private String hour;    // 인식된 시간의 시(HH)
        private String minute;  // 인식된 시간의 분(MM)
        private String second;  // 인식된 시간의 초(ss)
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BoundingPoly {
        private List<BoundingPolyVertex> vertices;

        @Data
        public static class BoundingPolyVertex {
            private Float x;
            private Float y;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MaskingPoly {
        private List<BoundingPoly.BoundingPolyVertex> vertices; // Masking Poly의 좌표 정보
    }
}
