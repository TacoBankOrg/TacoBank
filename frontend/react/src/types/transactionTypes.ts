export interface Transaction {
  tranAmt: undefined;
  tranNum: number; // 거래 고유 시퀀스 넘버
  type: string; // 거래 타입
  inoutType: string;
  printContent: string;
  balance?: number; // 거래 금액
  afterBalanceAmount: number; // 거래 후 잔액
  tranDateTime: string; // 거래 일시 (YYYY.mm.dd HH:mm:ss)
}

export interface TransactionReq {
  accountId: number;
  fromDate: string;
  toDate: string;
}