package com.taco_bank.business_server.domain.friend.model;


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
@Table(name = "friends")
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "BIGINT COMMENT '친구를 요청한 사람 ID'")
    private String requesterId;

    @Column(columnDefinition = "BIGINT COMMENT '친구를 요청을 받은 사람 ID'")
    private String receiverId;

    @Column(columnDefinition = "VARCHAR(1) COMMENT '즐겨찾기 여부'")
    private String liked;

    @Column(columnDefinition = "VARCHAR(30) COMMENT '상태'")
    private String status;

    @Column(columnDefinition = "DATETIME NOT NULL COMMENT '생성일자'")
    private LocalDateTime createdDate;

    @Column (columnDefinition = "DATETIME NOT NULL COMMENT '수정일자'")
    private LocalDateTime updatedDate;

    @PrePersist
    protected void onCreate() {
        this.createdDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedDate = LocalDateTime.now();
    }

    public Friend(String requesterId, String receiverId) {
        this.requesterId = requesterId;
        this.receiverId = receiverId;
        this.status = "NONE"; // 기본 상태 설정
        this.liked = "N"; // 기본 좋아요 상태 설정
        this.createdDate = LocalDateTime.now(); // 기본 생성일 설정
        this.updatedDate = LocalDateTime.now(); // 기본 수정일 설정
    }



}
