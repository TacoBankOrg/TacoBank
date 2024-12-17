package com.almagest_dev.tacobank_core_server.infrastructure.external.naver.dto.sms;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class NaverSmsRequestDto {
    private String type;                // SMS Type (SMS | LMS | MMS)
    private String contentType;         // 메시지 Type (COMM | AD)
    private String countryCode;         // 국가 번호 (Default: 82)
    private String from;                // 발신번호 (사전 등록된 발신번호만 사용 가능)
    // private String subject;          // 기본 메시지 제목	(LMS, MMS에서만 사용 가능)
    private String content;             // 기본 메시지 내용	(SMS: 최대 90byte)
    private List<Message> messages;     // 메시지 정보 리스트 (최대 100개)
    // private List<File> files;           // 파일 아이디 리스트 (MMS에서만 사용 가능)
    // private String reserveTime;         // 메시지 발송 예약 일시 (yyyy-MM-dd HH:mm)
    // private String reserveTimeZone;     // 예약 일시 타임존 (기본: Asia/Seoul)
}
