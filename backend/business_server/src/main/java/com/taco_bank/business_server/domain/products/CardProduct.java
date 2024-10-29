package com.taco_bank.business_server.domain.products;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "card_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 5)
    private String cardComCode;

    @Column(length = 100)
    private String cardName;

    @Column(length = 50)
    private String cardType;

    private Integer annualFee;

    @Column(length = 255)
    private String benefits;

    @Column(length = 255)
    private String condition;

    private Double pointRate;

    private Integer minPerformance;

    private Integer totalSubscribers;

    @Column(length = 1)
    private String onlyDomestic;

    private Double foreignPaymentFee;

    @Column(length = 50)
    private String issuanceTime;

    @Column(length = 255)
    private String description;
}
