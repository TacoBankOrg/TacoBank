package com.taco_bank.business_server.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "monthly_report")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    private Long totalSpent;

    @Column(length = 255)
    private String topSpentCategory;

    private Long totalAssetAmount;
    private Long totalCashAssetAmount;
    private Long totalInvestAssetAmount;
    private Long remainLoanAmount;
    private Long currentMonthBudget;

    @Column(length = 255)
    private String description;
}
