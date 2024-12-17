package com.almagest_dev.tacobank_core_server.domain.receipt.model;

import com.almagest_dev.tacobank_core_server.domain.settlememt.model.Settlement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "receipt")
public class Receipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(100) COMMENT 'API 호출 UUID'")
    private String apiRequestId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", columnDefinition = "BIGINT NULL COMMENT '정산 ID'")
    private Settlement settlement;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일시'")
    private LocalDateTime createdDate;

    @Column(columnDefinition = "DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일'")
    private LocalDateTime updatedDate;

    @OneToMany(mappedBy = "receipt")
    private List<ReceiptProduct> receiptProducts;

    /**
     * 생성자
     */
    public Receipt(String apiRequestId) {
        this.apiRequestId = apiRequestId;
        this.createdDate = LocalDateTime.now(); // 생성일시
        this.updatedDate = LocalDateTime.now(); // 수정일시 초기화
    }

    /**
     * Receipt Method
     */
    public static Receipt createReceipt(String apiRequestId) {
        return new Receipt(apiRequestId);
    }
    public void updateReceipt(Settlement settlement) {
        this.settlement = settlement;
        this.updatedDate = LocalDateTime.now();
    }
}
