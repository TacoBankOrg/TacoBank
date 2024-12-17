package com.almagest_dev.tacobank_core_server.infrastructure.external.testbed.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferApiRequestDto {
    private String userFinanceId;          // 필수: Y, 사용자 금융 ID
    private String transferPurpose;        // 필수: Y, 송금 목적 (예: "TR")
    private String tranAmt;                // 필수: Y, 거래 금액
    private String tranDtime;              // 필수: Y, 거래 일시 (예: "20190910101921")
    private String fintechUseNum;          // 필수: Y, 핀테크 사용 번호
    private String wdPrintContent;         // 필수: N, 출금 내역 출력 내용
    private String reqClientName;          // 필수: N, 요청자 이름
    private String reqClientBankCode;      // 필수: Y, 요청자 은행 코드
    private String reqClientAccountNum;    // 필수: Y, 요청자 계좌 번호
    private String recvAccountFintechUseNum; // 필수: Y, 수신 계좌 핀테크 사용 번호
    private String recvClientName;         // 필수: Y, 수신자 이름
    private String recvClientBankCode;     // 필수: Y, 수신자 은행 코드
    private String recvClientAccountNum;   // 필수: Y, 수신자 계좌 번호
    private String dpsPrintContent;        // 필수: N, 입금 내역 출력 내용
}
// 송금 요청
