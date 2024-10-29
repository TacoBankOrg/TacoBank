package com.taco_bank.business_server.domain.products;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "invest_isa_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvestIsaProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 50)
    private String type;

    @Column(length = 5)
    private String bankCode;

    @Column(length = 100)
    private String category;

    private LocalDateTime launchDate;

    @Column(length = 100)
    private String benefits;

    private Double joinFee;

    private Double manageFee;

    private Double terminationFee;

    private Double earlyWithdrawalFee;

    private Integer minBalance;

    private Integer maxBalance;

    @Column(length = 50)
    private String taxBenefitTerm;

    @Column(length = 50)
    private String managedInvestProducts;

    @Column(length = 10)
    private String riskLevel;

    @Column(length = 255)
    private String description;
}
