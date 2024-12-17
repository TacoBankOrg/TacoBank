package com.almagest_dev.tacobank_core_server.domain.settlememt.model;

import com.almagest_dev.tacobank_core_server.domain.group.model.GroupMember;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "settlement_details")
public class SettlementDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settlement_id", columnDefinition = "BIGINT NOT NULL COMMENT '정산ID'")
    private Settlement settlement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_member_id", columnDefinition = "BIGINT NOT NULL COMMENT '구성원 ID'")
    private GroupMember groupMember;

    @Column(name = "settlement_amount", columnDefinition = "BIGINT UNSIGNED NOT NULL COMMENT '개별 정산금액'")
    private Long settlementAmount;

    @Column(name = "settlement_status", columnDefinition = "VARCHAR(1) COMMENT '개별 정산상태'")
    private String settlementStatus;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일자'")
    private LocalDateTime createdDate;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '수정일자'")
    private LocalDateTime updatedDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    public void updateSettlementDetailsStatus(String settlementStatus) {
        this.settlementStatus = settlementStatus;
        this.updatedDate = LocalDateTime.now();
    }

    public void saveSettlement(Settlement settlement) {this.settlement = settlement;}
    public void saveGroupMember(GroupMember groupMember) {this.groupMember = groupMember;}
    public void saveSettlementAmount(long amount) {this.settlementAmount = amount;}
    public void saveSettlementStatus(String status) {this.settlementStatus = status;}
}
