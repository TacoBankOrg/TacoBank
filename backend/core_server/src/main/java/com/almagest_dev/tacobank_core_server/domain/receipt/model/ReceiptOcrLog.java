package com.almagest_dev.tacobank_core_server.domain.receipt.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "receipt_ocr_log")
public class ReceiptOcrLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(10) COMMENT '버전'")
    private String version;

    @Column(columnDefinition = "VARCHAR(100) COMMENT 'API 호출 UUID'")
    private String requestId;

    @Column(columnDefinition = "BIGINT COMMENT 'API 호출시각 (응답값)'")
    private Long timestamp;

    @Column(columnDefinition = "VARCHAR(100) COMMENT '이미지 UID'")
    private String imageUid;

    @Column(columnDefinition = "VARCHAR(255) COMMENT '이미지 이름'")
    private String imageName;

    @Column(columnDefinition = "VARCHAR(10) COMMENT '이미지 인식 결과 (SUCCESS, FAILURE, ERROR)'")
    private String imageInferResult;

    @Column(columnDefinition = "VARCHAR(255) COMMENT '결과 메시지'")
    private String imageMessage;

    @Column(columnDefinition = "VARCHAR(5) COMMENT 'OCR 추정 언어 (ko, en, ja)'")
    private String estimatedLanguage;

    @Column(columnDefinition = "TEXT COMMENT '요청 메시지 (JSON)'")
    private String jsonRequest;

    @Column(columnDefinition = "TEXT COMMENT '응답 메시지 (JSON)'")
    private String jsonResponse;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성일시'")
    private LocalDateTime createdDate;

    @Column(columnDefinition = "DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '수정일시'")
    private LocalDateTime updatedDate;

    /**
     * 생성자
     */
    // 최초 생성시
    public ReceiptOcrLog(String version, String requestId, String jsonRequest) {
        this.version = version;
        this.requestId = requestId;
        this.jsonRequest = jsonRequest;
        this.createdDate = LocalDateTime.now(); // 생성일시
        this.updatedDate = LocalDateTime.now(); // 수정일시 초기화
    }

    /**
     * ReceiptOcrLog Method
     */
    public static ReceiptOcrLog createReceiptOcrLog(String version,
                                                    String requestId,
                                                    String jsonRequest) {
        return new ReceiptOcrLog(version, requestId, jsonRequest);
    }

    public void updateReceiptOcrLog(Long timestamp,
                                    String imageUid,
                                    String imageName,
                                    String imageInferResult,
                                    String imageMessage,
                                    String estimatedLanguage,
                                    String jsonResponse) {
        this.timestamp = timestamp;
        this.imageUid = imageUid;
        this.imageName = imageName;
        this.imageInferResult = imageInferResult;
        this.imageMessage = imageMessage;
        this.estimatedLanguage = estimatedLanguage;
        this.jsonResponse = jsonResponse;
        this.updatedDate = LocalDateTime.now(); // 수정일시 갱신
    }
}
