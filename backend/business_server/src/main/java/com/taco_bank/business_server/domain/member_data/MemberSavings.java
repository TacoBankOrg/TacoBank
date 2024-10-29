package com.taco_bank.business_server.domain.member_data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "member_savings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberSavings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long savingsId;

    private Long monthlyDeposit;

    @Column(length = 1)
    private String autoRenewal;

    private Long currentBalance;

    private LocalDateTime joinDate;

    private LocalDateTime endDate;

    private Integer remainDepositCount;
}
