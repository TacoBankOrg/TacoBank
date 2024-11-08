package com.taco_bank.business_server.domain.group.model;

import com.taco_bank.business_server.domain.member.model.Member;
import com.taco_bank.business_server.domain.member.model.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "pay_group")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", columnDefinition = "BIGINT NOT NULL COMMENT '그룹장 멤버 ID'")
    private Member leader;

    @Column(columnDefinition = "VARCHAR(50) COMMENT '그룹 이름'")
    private String name;

    @Column(columnDefinition = "VARCHAR(1) COMMENT '활성화 상태'")
    private String activated;

    @Column(columnDefinition = "VARCHAR(1) COMMENT '커스텀 여부'")
    private String customized;

    @Column(columnDefinition = "DATETIME NOT NULL COMMENT '생성일자'")
    private LocalDateTime createdDate;

    @Column (columnDefinition = "DATETIME NOT NULL COMMENT '수정일자'")
    private LocalDateTime updatedDate;

    @OneToMany(mappedBy = "payGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMember> payGroups; // 그룹 구성원의 정산그룹 ID

}
