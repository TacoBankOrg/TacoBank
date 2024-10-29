package com.taco_bank.business_server.domain.products;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "savings_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavingsProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String savingsName;

    @Column(length = 10)
    private String savingsType;

    @Column(length = 5)
    private String bankCode;

    private Double interestRate;

    private Integer maxMonthlyDeposit;

    @Column(length = 1)
    private String autoRenewal;

    private Double earlyWithdrawalFee;

    @Column(length = 255)
    private String description;
}
