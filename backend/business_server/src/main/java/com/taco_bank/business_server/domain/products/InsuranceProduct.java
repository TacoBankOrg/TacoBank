package com.taco_bank.business_server.domain.products;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "insurance_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 5)
    private String insuranceComCode;

    @Column(length = 100)
    private String insuranceName;

    @Column(length = 50)
    private String contractType;

    @Column(length = 100)
    private String category;

    @Column(length = 100)
    private String subCategory;

    @Column(length = 255)
    private String coverageDetail;

    private Integer coverageAmount;

    @Column(length = 255)
    private String condition;

    @Column(length = 50)
    private String paymentType;

    private Integer paymentAmount;

    @Column(length = 50)
    private String paymentPeriod;

    @Column(length = 50)
    private String totalPaymentPeriod;

    @Column(length = 255)
    private String speciality;

    @Column(length = 255)
    private String taxBenefit;

    private Integer totalSubscription;

    private Double surrenderRefundRate;

    @Column(length = 255)
    private String description;
}
