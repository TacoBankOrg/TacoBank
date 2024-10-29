package com.taco_bank.business_server.domain.products;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "invest_pension_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvestPensionProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String pensionName;

    @Column(length = 5)
    private String manageComCode;

    @Column(length = 20)
    private String type;

    private Double interestRate;

    private Integer maturityMonth;

    private Integer minDeposit;

    private Integer maxDeposit;

    @Column(length = 1)
    private String principalGuarantee;

    @Column(length = 50)
    private String depositPeriod;

    @Column(length = 50)
    private String payoutType;

    @Column(length = 100)
    private String manageType;

    @Column(length = 100)
    private String taxBenefit;

    @Column(length = 50)
    private String taxBenefitTerm;

    private Double earlyWithdrawalFee;

    private LocalDateTime onDate;

    private LocalDateTime launchDate;

    @Column(length = 10)
    private String riskLevel;

    @Column(length = 255)
    private String description;
}
