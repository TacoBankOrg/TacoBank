package com.almagest_dev.tacobank_core_server.domain.account.model;

import com.almagest_dev.tacobank_core_server.domain.member.model.Member;
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
@Table(name = "favorite_account")
public class FavoriteAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", columnDefinition = "BIGINT NOT NULL COMMENT '멤버 ID'")
    private Member member;

    @Column(columnDefinition = "VARCHAR(20) COMMENT '계좌번호'")
    private String accountNum;

    @Column(columnDefinition = "VARCHAR(20) COMMENT '예금주'")
    private String accountHolderName;

    @Column(columnDefinition = "VARCHAR(5) COMMENT '은행코드'")
    private String bankCode;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '등록일자'")
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

    public void saveMember(Member member) {this.member = member;}
    public void saveAccountNum(String accountNum) {this.accountNum = accountNum;}
    public void saveAccountHolderName(String name) {this.accountHolderName = name;}
    public void saveBankCode(String bankCode) {this.bankCode = bankCode;}
}
