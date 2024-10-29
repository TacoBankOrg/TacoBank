package com.taco_bank.business_server.domain.products;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "loan_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String loanName;

    @Column(length = 100)
    private String targetGroup;

    private Integer targetAge;

    @Column(length = 50)
    private String targetRegion;

    private Double interestRate;

    @Column(length = 255)
    private String loanOrganList;

    private Long loanLimit;

    @Column(length = 100)
    private String repaymentType;

    @Column(length = 50)
    private String maxLoanTerm;

    @Column(length = 50)
    private String maxExtensionTerm;

    private Integer minCreditScore;

    private Integer minIncome;

    private Integer houseCount;

    private LocalDateTime onDate;

    @Column(length = 50)
    private String usageFor;

    @Column(length = 255)
    private String description;
}
