package com.taco_bank.business_server.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "code_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CodeMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 5)
    private String code;

    @Column(length = 100)
    private String name;
}
