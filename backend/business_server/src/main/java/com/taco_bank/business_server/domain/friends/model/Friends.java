package com.taco_bank.business_server.domain.friends.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "firends")
public class Friends {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "BIGINT NOT NULL COMMENT '요청자 ID'")
    private Long requesterId;

    @Column(columnDefinition = "BIGINT NOT NULL COMMENT '친구 ID'")
    private Long receiverId;

    @Column(columnDefinition = "VARCHAR(1) NOT NULL COMMENT '즐겨찾기 여부'")
    private String liked;

    @Column(columnDefinition = "VARCHAR(1) NOT NULL COMMENT '추가 여부'")
    private String requested;

    @Column(columnDefinition = "VARCHAR(1) NOT NULL COMMENT '수락 여부'")
    private String accepted;

    @Column(columnDefinition = "VARCHAR(1) NOT NULL COMMENT '차단 여부'")
    private String banded;

    @Column(columnDefinition = "VARCHAR(1) NOT NULL COMMENT '삭제 여부'")
    private String deleted;

    @Column(columnDefinition = "DATETIME NOT NULL COMMENT '생성일자'")
    private LocalDateTime createdDate;

    @Column(columnDefinition = "DATETIME NOT NULL COMMENT '수정일자'")
    private LocalDateTime updatedDate;
}
