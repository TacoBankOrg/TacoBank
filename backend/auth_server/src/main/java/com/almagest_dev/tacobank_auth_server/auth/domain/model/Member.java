package com.almagest_dev.tacobank_auth_server.auth.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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

    @Column(columnDefinition = "VARCHAR(255) COMMENT '출금 비밀번호'")
    private String transferPin;

    @Column(columnDefinition = "VARCHAR(1) DEFAULT 'N' NOT NULL COMMENT '탈퇴 여부(탈퇴시, Y)'")
    private String deleted;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", columnDefinition = "BIGINT COMMENT '권한 ID'")
    private Role role;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '가입일자'")
    private LocalDateTime createdDate;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NOT NULL COMMENT '수정일자'")
    private LocalDateTime updatedDate;

    /**
     * 일자 관련 세팅
     */
    @PrePersist
    public void prePersist() {
        if (this.createdDate == null) {
            this.createdDate = LocalDateTime.now();
        }
        this.updatedDate = LocalDateTime.now(); // 최초 생성시에도 수정일자 생성
    }

    /**
     * Member 관련 메서드
     */
    public static Member createMember(String email,
                                      String password,
                                      String name,
                                      String birth,
                                      String tel,
                                      Role role) {
        return new Member(
                null,                // id는 자동 생성
                null,                   // 최초 생성시에는 사용자 고유 번호 없음
                email,                  // 이메일
                password,               // 비밀번호
                name,                   // 이름
                birth,                  // 생년월일
                tel,                    // 전화번호
                "N",                    // 최초 계좌 연동 여부
                null,                   // 출금 비밀번호
                "N",                    // 탈퇴 여부 (초기값: N)
                role,                   // 권한
                LocalDateTime.now(),    // 가입일자
                LocalDateTime.now()     // 수정일자
        );
    }
}
