package com.taco_bank.business_server.domain.member_data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "member_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberCards {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long cardId;

    @Column(length = 20)
    private String cardNumber;

    private LocalDateTime issuedDate;

    private LocalDateTime expiredDate;

    private LocalDateTime withdrawalDate;

    @Column(length = 20)
    private String withdrawalAccountNumber;

    @Column(length = 5)
    private String withdrawalBankCode;
}
