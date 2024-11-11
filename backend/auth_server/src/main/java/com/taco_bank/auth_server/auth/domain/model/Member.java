package com.taco_bank.auth_server.auth.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(columnDefinition = "VARCHAR(40) COMMENT '사용자 금융 식별번호'")
    private String userFinanceId;

    @Column(columnDefinition = "VARCHAR(100) NOT NULL COMMENT '이메일(계정 아이디)'")
    private String email;

    @Column(columnDefinition = "VARCHAR(255) NOT NULL COMMENT '비밀번호'")
    private String password;

    @Column(columnDefinition = "VARCHAR(20) NOT NULL COMMENT '이름'")
    private String name;

    @Column(columnDefinition = "VARCHAR(20) NOT NULL COMMENT '전화번호'")
    private String tel;

    @Column(columnDefinition = "VARCHAR(1) DEFAULT 'N' NOT NULL COMMENT '탈퇴 여부(탈퇴시, Y)'")
    private String deleted;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", columnDefinition = "BIGINT COMMENT '권한 ID'")
    private Role role;

    /**
     * Member 관련 메서드
     */
    public static Member createMember(String email, String password, String name, String tel, Role role) {
        Member member = new Member();
        member.email = email;
        member.password = password;
        member.name = name;
        member.tel = tel;
        member.role = role;
        member.deleted = "N";
        return member;
    }
}
