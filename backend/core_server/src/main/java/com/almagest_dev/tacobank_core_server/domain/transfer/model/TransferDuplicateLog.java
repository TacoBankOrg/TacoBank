package com.almagest_dev.tacobank_core_server.domain.transfer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "transfer_duplicate_log")
public class TransferDuplicateLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(40) NOT NULL COMMENT '거래 고유 ID'")
    private String transactionId;

    @Column(columnDefinition = "VARCHAR(40) NOT NULL COMMENT '중복 방지 KEY(클라이언트에서 생성)'")
    private String idempotencyKey;

    @Column(columnDefinition = "BIGINT COMMENT '정산 ID'")
    private Long settlementId;

    @Column(columnDefinition = "BIGINT COMMENT '출금 멤버 ID'")
    private Long memberId;

    @Column(columnDefinition = "BIGINT COMMENT '출금 계좌 ID'")
    private Long accountId;

    @Column(columnDefinition = "VARCHAR(5) NOT NULL COMMENT '출금 은행코드'")
    private String withdrawalBankCode;

    @Column(columnDefinition = "VARCHAR(20) NOT NULL COMMENT '출금 계좌번호'")
    private String withdrawalAccountNum;

    @Column(columnDefinition = "VARCHAR(20) NOT NULL COMMENT '출금 예금주'")
    private String withdrawalAccountHolder;

    @Column(columnDefinition = "VARCHAR(5) NOT NULL COMMENT '수신 은행코드'")
    private String receiverBankCode;

    @Column(columnDefinition = "VARCHAR(20) NOT NULL COMMENT '수신 계좌번호'")
    private String receiverAccountNum;

    @Column(columnDefinition = "VARCHAR(20) NOT NULL COMMENT '수신 예금주'")
    private String receiverAccountHolder;

    @Column(columnDefinition = "INT NOT NULL COMMENT '거래 금액'")
    private Integer amount;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '로그 생성일시'")
    private LocalDateTime createdDate;

    /**
     * TransferDuplicateLog 관련 메서드
     */
    public static TransferDuplicateLog createTransferDuplicateLog(
            String transactionId,
            String idempotencyKey,
            Long settlementId,
            Long memberId,
            Long accountId,
            String withdrawalBankCode,
            String withdrawalAccountNum,
            String withdrawalAccountHolder,
            String receiverBankCode,
            String receiverAccountNum,
            String receiverAccountHolder,
            Integer amount
    ) {
        return new TransferDuplicateLog(
                null, // ID는 자동 생성됨
                transactionId,
                idempotencyKey,
                settlementId,
                memberId,
                accountId,
                withdrawalBankCode,
                withdrawalAccountNum,
                withdrawalAccountHolder,
                receiverBankCode,
                receiverAccountNum,
                receiverAccountHolder,
                amount,
                LocalDateTime.now() // 현재 시간으로 설정
        );
    }

}
