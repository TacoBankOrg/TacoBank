package com.taco_bank.business_server.domain.member_data;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "member_isa")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberIsa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long isaId;

    private Integer currentBalance;

    private Double totalProfitRate;

    private LocalDateTime joinDate;
}
