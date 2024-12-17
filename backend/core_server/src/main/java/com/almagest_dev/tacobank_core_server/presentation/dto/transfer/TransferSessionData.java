package com.almagest_dev.tacobank_core_server.presentation.dto.transfer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransferSessionData {
    private String idempotencyKey;              // 중복 방지 키(클라이언트에서 생성)
    private Long memberId;                    // 사용자 ID
    private Long settlementId; // 정산(바로 송금)인 경우 필수

    private String withdrawalUserFinanceId;     // 사용자 금융 식별번호
    private String withdrawalFintechUseNum;     // 출금 계좌 핀테크 이용번호

    private WithdrawalDetails withdrawalDetails; // 출금 정보 (계좌 ID, 계좌 번호, 예금주, 은행 코드)

    private String receiverFintechUseNum;   // 입금(수취) 계좌 핀테크 이용번호
    private ReceiverDetails receiverDetails; // 입금(수취) 정보 (계좌 번호, 예금주, 은행 코드)

    private int amount;                     // 송금액
    private boolean passwordVerified;       // 결제 비밀번호 검증 여부

    public void assignReceiverFintechUseNum(String receiverFintechUseNum) {
        this.receiverFintechUseNum = receiverFintechUseNum;
    }
    public void assignAmount(int amount) {
        this.amount = amount;
    }
    public void changePasswordVerified(boolean flag) {
        this.passwordVerified = flag;
    }
    public boolean isPasswordVerified() {
        return this.passwordVerified;
    }
}
