package com.taco_bank.business_server.domain.member_data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "member_stocks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberStocks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(length = 50)
    private String stockName;

    @Column(length = 5)
    private String stockCode;

    @Column(length = 100)
    private String category;

    private Double dividendRate;

    private Integer currentPrice;

    private Integer buyPrice;

    private Integer count;
}
