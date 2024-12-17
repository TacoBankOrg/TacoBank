//홈화면 정보 요청
export interface HomeReq {
    memberId: number;
}

export interface MainAccount {
  memberId: number
  accountId: number
}

//홈화면 정보 응답
export interface HomeRsp {
    memberId: number;
    email: string;
    name: string;
    tel: string;
    mainAccountId: number;
    pinSet: boolean;
    mydataLinked: string
    accountList: Account[];
}

//계좌목록
export interface Account {
    accountId: number; // 계좌 식별자
    accountName: string; // 계좌명
    balance: number; // 잔고
    bankCode: string;
    bankName: string; // 은행명
    accountNum: string; // 계좌 번호
    accountHolder: string; // 예금주
    transactionList: Transaction[];
  }
  //계좌별 거래목록
  export interface Transaction {
    tranNum: number; // 거래 고유 시퀀스 넘버
    type?: string; // 거래 타입
      inoutType: string;
      printContent: string
    tranAmt: number; // 거래 금액
    afterBalanceAmount?: number; // 거래 후 잔액
    tranDateTime: string; // 거래 일시 (YYYY.mm.dd HH:mm:ss)
  }

//PIN 생성 요청
export interface PinReq {
    memberId: number;
    transferPin: string;
    confirmTransferPin: string;
}

//sms요청
export interface SmsVerificationReq {
    tel: string; // 전화번호
    type: string;
  }

  //sms 요청 응답
  export interface SmsVerificationRsp {
    verificationId: number;
  }

  //sms 검증 요청
  export interface SmsConfirmReq{
    tel: string;
    verificationId: number;
    inputCode: string;
  }