package com.taco_bank.business_server.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "member_info")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    private Integer age;

    @Column(length = 10)
    private String sex;

    @Column(length = 50)
    private String region;

    private Long income;

    @Column(length = 50)
    private String job;

    @Column(length = 50)
    private String investType;

    @Column(length = 50)
    private String preferProduct;
}
