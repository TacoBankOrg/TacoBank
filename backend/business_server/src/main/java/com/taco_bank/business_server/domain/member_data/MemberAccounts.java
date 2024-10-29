package com.taco_bank.business_server.domain.member_data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "member_accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberAccounts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(length = 5)
    private String bankCode;

    @Column(length = 20)
    private String accountNumber;

    @Column(length = 20)
    private String holderName;

    private LocalDateTime joinDate;
}
