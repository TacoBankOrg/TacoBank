package com.taco_bank.business_server.domain;

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

    @Column(nullable = false)
    private Long roleId;

    @Column(length = 50)
    private String name;

    @Column(length = 100)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(length = 20)
    private String tel;

    // 관계 정의 필요(Role과의 관계 등).
}
