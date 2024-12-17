export interface CheckReceiverReq {
  idempotencyKey: string;
  settlementId?: number
  withdrawalMemberId: number;
  withdrawalAccountId: number;
  receiverBankCode: string;
  receiverAccountNum: string;
}

export interface CheckReceiverRep {
  idempotencyKey: string;
  settlementId?: number
  receiverAccountHolder: string;
  withdrawalAccountId: number;
  withdrawalAccountNum: string;
  withdrawalBalance: number;
}

export interface BaroTransferReq {
  idempotencyKey: string;
  memberId: number;
  settlementId: number;
  amount: number;
}

export interface Account {
  accountId?: number;
  accountHolder: string;
  accountNum: string;
  bankName: string;
  bankCode: string
  balance?: number;
}

export interface BaroTransferRep {
  idempotencyKey: string;
  accountInfoWithBalances: Account[];
}

export interface TransferReq {
  idempotencyKey: string;
  memberId: number;
  settlementId: number | null;
  withdrawalDetails: {
    accountId: number;
    accountNum: string;
    accountHolder: string;
    bankCode: string;
  };
  receiverDetails: {
    accountNum: string; // 입금 계좌번호
    accountHolder: string; // 입금계좌 예금주
    bankCode: string; // 입금 은행 코드
  };
  transferPin: string;
  amount: number;
  rcvPrintContent: string; // 출금 내역 출력 문구(출금계좌표시)
  wdPrintContent: string;
}

export interface TransferRep {
  idempotencyKey: string;
  tranDtm: string;
  memberId: number;
  withdrawalDetails: {
    accountId: number;
    accountNum: string;
    accountHolder: string;
    bankCode: string;
  };
  receiverDetails: {
    accountNum: string; // 입금 계좌번호
    accountHolder: string; // 입금계좌 예금주
    bankCode: string; // 입금 은행 코드
  };
  amount: number;
}

export interface AccountsData {
  favoriteAccounts: Account[]; // 즐겨찾기 계좌
  recentAccounts: Account[];   // 최근 계좌
  friendsAccounts: Account[];  // 친구 계좌
}