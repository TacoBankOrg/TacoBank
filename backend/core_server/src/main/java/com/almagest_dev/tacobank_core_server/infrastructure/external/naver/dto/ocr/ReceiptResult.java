package com.almagest_dev.tacobank_core_server.infrastructure.external.naver.dto.ocr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReceiptResult {
    private StoreInfo storeInfo;        // 점포 정보
    private PaymentInfo paymentInfo;    // 결제 정보
    private List<SubResult> subResults; // 상품 그룹 정보
    private List<SubTotal> subTotal;    // 총합계 정보
    private TotalPrice totalPrice;      // 총 금액 정보
    
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class StoreInfo {
        private BaseDetail name;
        private BaseDetail subName;
        private BaseDetail bizNum;
        private BaseDetail movieName;
        private List<BaseDetail> addresses; // 점포 주소 상세 정보
        private List<BaseDetail> tel;       // 점포 전화번호 상세 정보
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaymentInfo {
        private BaseDetail date;        // 결제 일자 상세 정보
        private BaseDetail time;        // 결제 시간 상세 정보
        private CardInfo cardInfo;      // 결제 카드 정보
        private BaseDetail confirmNum;  // 승인 번호 상세 정보
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        static class CardInfo {
            private BaseDetail company; // 카드사 상세 정보
            private BaseDetail number;  // 카드 번호 상세 정보
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SubResult {
        private List<Item> items;   // 인식된 상품 상세 정보
        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Item {
            private BaseDetail name;    // 항목 이름 상세 정보
            private BaseDetail code;    // 항목 코드 상세 정보
            private BaseDetail count;   // 항목 수량 상세 정보
            private Price price;        // 항목 가격 정보

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public static class Price {
                private BaseDetail price;         // 항목 가격 상세 정보
                private BaseDetail unitPrice;     // 항목 단가 상세 정보
            }
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SubTotal {
        private List<BaseDetail> taxPrice;      // 부과세 상세 정보
        private List<BaseDetail> discountPrice; // 할인 금액 상세 정보
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TotalPrice {
        private BaseDetail price; // 총 금액 상세 정보
    }
}
