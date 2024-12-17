package com.almagest_dev.tacobank_core_server.domain.bankCode;

import com.almagest_dev.tacobank_core_server.domain.member.model.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "org_code")
public class OrgCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(5) COMMENT '은행 코드'")
    private String code;

    @Column(columnDefinition = "VARCHAR(100) COMMENT '기관명'")
    private String name;

    @Column(columnDefinition = "VARCHAR(100) COMMENT '계좌 번호 길이'")
    private String accountLength;

}
