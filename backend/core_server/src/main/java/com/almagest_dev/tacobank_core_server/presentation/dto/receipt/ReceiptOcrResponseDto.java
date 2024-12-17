package com.almagest_dev.tacobank_core_server.presentation.dto.receipt;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder(toBuilder = true)
public class ReceiptOcrResponseDto {
    private long receiptId;   // 영수증 ID
    private int totalAmount;  // 총합계 금액
    private List<Item> items; // 품목 리스트

    @Getter
    @Builder
    public static class Item {
        private long productId;     // 품목 ID
        private int number;       // 품목 번호
        private String name;      // 품목 이름
        private int totalPrice;   // 품목총 가격

        @Override
        public String toString() {
            return "Item{" +
                    "productId=" + productId +
                    ", number=" + number +
                    ", name='" + name + '\'' +
                    ", totalPrice=" + totalPrice +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "ReceiptOcrResponseDto{" +
                "receiptId=" + receiptId +
                ", totalAmount=" + totalAmount +
                ", items=" + items +
                '}';
    }
}
