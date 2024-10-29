package com.taco_bank.business_server.domain.products;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "invest_fund_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvestFundProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String fundName;

    @Column(length = 5)
    private String manageComCode;

    @Column(length = 100)
    private String category;

    @Column(length = 100)
    private String subCategory;

    private Double manageFee;

    @Column(length = 10)
    private String investRegion;

    @Column(length = 1)
    private String dividends;

    private Double totalProfits;

    private Long minInvestAmount;

    private Long maxInvestAmount;

    @Column(length = 10)
    private String riskLevel;

    private LocalDateTime endDate;

    private LocalDateTime startDate;

    @Column(length = 255)
    private String description;
}
