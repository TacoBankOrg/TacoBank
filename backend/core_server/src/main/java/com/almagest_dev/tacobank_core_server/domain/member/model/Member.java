package com.almagest_dev.tacobank_core_server.domain.member.model;

import com.almagest_dev.tacobank_core_server.domain.account.model.MainAccount;
import com.almagest_dev.tacobank_core_server.domain.group.model.Group;
import com.almagest_dev.tacobank_core_server.domain.group.model.GroupMember;
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
@Table(name = "member")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(columnDefinition = "VARCHAR(40) COMMENT '사용자 금융 식별번호'")
    private String userFinanceId;

    @Column(columnDefinition = "VARCHAR(100) NOT NULL COMMENT '이메일(계정 아이디)'")
    private String email;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL COMMENT '비밀번호'")
    private String password;

    @Column(columnDefinition = "VARCHAR(20) NOT NULL COMMENT '이름'")
    private String name;

    @Column(columnDefinition = "VARCHAR(10) NOT NULL COMMENT '생년월일(yyMMdd)'")
    private String birth;

    @Column(columnDefinition = "VARCHAR(20) NOT NULL COMMENT '전화번호'")
    private String tel;

    @Column(columnDefinition = "VARCHAR(1) NOT NULL COMMENT '최초 계좌연동 여부(Y, N)'")
    private String mydataLinked;

    @Column(columnDefinition = "VARCHAR(1) DEFAULT 'N' NOT NULL COMMENT '탈퇴 여부(탈퇴시, Y)'")
    private String deleted;

    @Column(columnDefinition = "VARCHAR(255) COMMENT '출금 비밀번호'")
    private String transferPin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", columnDefinition = "BIGINT COMMENT '권한 ID'")
    private Role role;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '가입일자'")
    private LocalDateTime createdDate;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '수정일자'")
    private LocalDateTime updatedDate;

    @OneToMany(mappedBy = "leader", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Group> groups;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GroupMember> groupMembers; // 그룹 구성원의 멤버 ID

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MainAccount mainAccount;

    /**
     * 관련 메서드
     */

    // 회원 탈퇴 처리(비활성화)
    public void deactivate() {
        this.deleted = "Y";
        this.email = this.email + "_OUT";
        this.tel = this.tel + "_OUT";
    }

    // 회원 정보 수정, 값 변경
    public void changeName(String name) {
        this.name = name;
    }
    public void changeTel(String tel) {
        this.tel = tel;
    }
    public void changePassword(String password) {
        this.password = password;
    }
    public void changeTransferPin(String transferPin) {
        this.transferPin = transferPin;
    }

    public void changeMydataLinked(String mydataLinked) {
        this.mydataLinked = mydataLinked;
    }

    public Long getMainAccountId() {
        return mainAccount != null ? mainAccount.getId() : null;
    }

}
