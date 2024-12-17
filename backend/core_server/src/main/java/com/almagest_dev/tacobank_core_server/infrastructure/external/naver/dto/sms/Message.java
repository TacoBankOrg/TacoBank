package com.almagest_dev.tacobank_core_server.infrastructure.external.naver.dto.sms;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {
    private String to;      // 수신번호 (붙임표 ( - )를 제외한 숫자만 입력 가능)
    private String subject; // 개별 메시지 제목 (LMS, MMS에서만 사용 가능)
    private String content; // 개별 메시지 내용	(SMS: 최대 90byte)

    public void createMessage(String tel, String code) {
        this.to = tel;
        this.content = "[TacoBank] 인증번호: " + code + "\n타인 유출로 인한 피해 주의";
    }
}
