package com.almagest_dev.tacobank_core_server.domain.group.model;


import com.almagest_dev.tacobank_core_server.domain.member.model.Member;
import com.almagest_dev.tacobank_core_server.domain.receipt.model.ReceiptMember;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pay_group_member")
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pay_group_id", columnDefinition = "BIGINT NOT NULL COMMENT '정산 그룹 ID'")
    private Group payGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", columnDefinition = "BIGINT NOT NULL COMMENT '멤버 ID'")
    private Member member;

    @Column(columnDefinition = "VARCHAR(30) COMMENT '상태'")
    private String status;

    @OneToMany(mappedBy = "groupMember")
    private List<ReceiptMember> receiptMembers;

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

    public void saveGroup(Group group) {
        this.payGroup = group;
    }
    public void saveStatus(String invited) {
        this.status = invited;
    }
    public void savePayGroup(Group group) {
        this.payGroup = group;
    }
    public void saveMember(Member member) {
        this.member = member;
    }
}
