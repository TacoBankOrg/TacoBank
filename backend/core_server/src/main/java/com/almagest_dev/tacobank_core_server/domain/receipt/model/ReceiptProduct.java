package com.almagest_dev.tacobank_core_server.domain.receipt.model;

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
@Table(name = "receipt_product")
public class ReceiptProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", columnDefinition = "BIGINT COMMENT '영수증 ID'")
    private Receipt receipt;

    @Column(columnDefinition = "VARCHAR(255) COMMENT '품목 이름'")
    private String name;

    @Column(columnDefinition = "INT COMMENT '품목 개수'")
    private Integer count;

    @Column(columnDefinition = "INT COMMENT '품목 단가'")
    private Integer unitPrice;

    @Column(columnDefinition = "INT COMMENT '품목 총액'")
    private Integer totalPrice;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일시'")
    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "receiptProduct")
    private List<ReceiptMember> receiptMembers;

    /**
     * 생성자
     */
    public ReceiptProduct(Receipt receipt, String name, Integer count, Integer unitPrice, Integer totalPrice) {
        this.receipt = receipt;
        this.name = name;
        this.count = count;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.createdDate = LocalDateTime.now(); // 생성일시
    }

    /**
     * ReceiptProduct Method
     */
    public static ReceiptProduct createReceiptProduct(Receipt receipt, String name, Integer count, Integer unitPrice, Integer totalPrice) {
        return new ReceiptProduct(receipt, name, count, unitPrice, totalPrice);
    }
}
