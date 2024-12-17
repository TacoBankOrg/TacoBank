package com.almagest_dev.tacobank_core_server.domain.receipt.model;

import com.almagest_dev.tacobank_core_server.domain.group.model.GroupMember;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "receipt_member")
public class ReceiptMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_product_id", columnDefinition = "BIGINT COMMENT '영수증 품목 ID'")
    private ReceiptProduct receiptProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_member_id", columnDefinition = "BIGINT COMMENT '구성원 ID'")
    private GroupMember groupMember;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일시'")
    private LocalDateTime createdDate;

    /**
     * 생성자
     */
    public ReceiptMember(ReceiptProduct receiptProduct, GroupMember groupMember) {
        this.receiptProduct = receiptProduct;
        this.groupMember = groupMember;
        this.createdDate = LocalDateTime.now(); // 생성일시
    }

    /**
     * ReceiptMember Method
     */
    public static ReceiptMember createReceiptMember(ReceiptProduct receiptProduct, GroupMember groupMember) {
        return new ReceiptMember(receiptProduct, groupMember);
    }
}
