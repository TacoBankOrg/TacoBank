package com.almagest_dev.tacobank_core_server.domain.sms.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "sms_verification_log")
public class SmsVerificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "VARCHAR(10) COMMENT '요청 종류(회원가입: join | 비밀번호: pw | 출금 비밀번호: pin | 통합계좌연결: mydata | 휴대전화번호 수정: tel)'")
    private String requestType;

    @Column(columnDefinition = "VARCHAR(100) COMMENT '요청 ID (API 응답값)'")
    private String requestId;

    @Column(columnDefinition = "DATETIME COMMENT '요청 시간 (API 응답값)'")
    private LocalDateTime requestTime;

    @Column(columnDefinition = "VARCHAR(10) COMMENT '요청 상태 코드'")
    private String statusCode;

    @Column(columnDefinition = "VARCHAR(20) COMMENT '요청 상태명 (success | fail)'")
    private String statusName;

    @Column(columnDefinition = "VARCHAR(20) COMMENT '수신 번호'")
    private String receiverTel;

    @Column(columnDefinition = "VARCHAR(10) COMMENT '사용자가 입력한 인증 번호'")
    private String inputCode;

    @Column(columnDefinition = "VARCHAR(10) COMMENT '인증 번호'")
    private String verificationCode;

    @Column(columnDefinition = "VARCHAR(10) COMMENT '인증 상태(REQUEST | VERIFIED | FAIL)'")
    private String verificationStatus;

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
    public SmsVerificationLog(String requestType, String receiverTel, String verificationCode, String jsonRequest) {
        this.requestType = requestType;
        this.receiverTel = receiverTel;
        this.verificationCode = verificationCode;
        this.verificationStatus = "REQUEST";
        this.jsonRequest = jsonRequest;
        this.createdDate = LocalDateTime.now(); // 생성일시
        this.updatedDate = LocalDateTime.now(); // 수정일시 초기화
    }

    /**
     * SmsVerificationLog Method
     */
    public static SmsVerificationLog createSmsVerificationLog(String requestType, String receiverTel,
                                                         String verificationCode,
                                                         String jsonRequest) {
        return new SmsVerificationLog(requestType, receiverTel, verificationCode, jsonRequest);
    }

    public void updateResponseSmsVerificationLog(String requestId, LocalDateTime requestTime,
                                                 String statusCode, String statusName, String jsonResponse) {
        this.requestId = requestId;
        this.requestTime = requestTime;
        this.statusCode = statusCode;
        this.statusName = statusName;
        this.jsonResponse = jsonResponse;
        this.updatedDate = LocalDateTime.now(); // 수정일시 갱신
    }

    public void updateVerificationStatusAndInputCode(String verificationStatus, String inputCode) {
        this.verificationStatus = verificationStatus;
        this.inputCode = inputCode;
        this.updatedDate = LocalDateTime.now(); // 수정일시 갱신
    }
}
