package com.taco_bank.business_server.domain.member_data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "member_loan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberLoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long loanId;

    private LocalDateTime interestWithdrawalDate;

    private LocalDateTime joinDate;

    private LocalDateTime endDate;
}
