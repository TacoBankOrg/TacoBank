package com.taco_bank.business_server.domain.group.model;

import com.taco_bank.business_server.domain.member.model.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
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

    @Column(columnDefinition = "VARCHAR(1) COMMENT '초대 여부'")
    private String invited;

    @Column(columnDefinition = "VARCHAR(1) COMMENT '승인 여부'")
    private String accepted;

    @Column(columnDefinition = "VARCHAR(1) COMMENT '차단 여부'")
    private String banned;

    @Column(columnDefinition = "VARCHAR(1) COMMENT '탈퇴 여부'")
    private String exited;

    @Column(columnDefinition = "DATETIME NOT NULL COMMENT '생성일자'")
    private LocalDateTime createdDate;

    @Column (columnDefinition = "DATETIME NOT NULL COMMENT '수정일자'")
    private LocalDateTime updatedDate;


}
