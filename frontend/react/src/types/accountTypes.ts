import { Transaction } from "./transactionTypes";

export interface Account {
  accountId: number; // 계좌 식별자
  accountName: string; // 계좌명
  balance: number; // 잔고
  bankCode: string; // 은행 코드
  bankName: string; // 은행명
  accountNum: string; // 계좌 번호
  accountHolder: string; // 예금주
  transactionList?: Transaction[];
}

export interface MyAccountRep {
  accountId: number;
  accountName: string;
  accountNum: string;
  accountHolder: string;
  bankName: string;
  bankCode: string;
  balance: number;
  transactionList: null;
}

export interface FavoriteAccount {
  accountId: number; // 즐겨찾기 계좌 ID
  memberId?: number; // 회원 ID
  accountNumber: string; // 계좌번호
  accountHolder: string; // 예금주
  bankCode: number; // 은행코드
}

export interface FavoriteAccountReq {
  memberId: number;
  accountNum: string;
}

export interface MyAccount {
  accountId: number; // 계좌 ID
  accountHolder: string; // 예금주
  accountNumber: string; // 계좌번호
  bankName: string; // 은행명
}

export interface SmsVerificationRequestDto {
  type: string;
  tel: string; // 전화번호
}

export interface SmsVerificationResponseDto {
  verificationId: number;
}

export interface SmsConfirmRequestDto {
  memberId?: number
  type: string;
  tel: string;
  verificationId: number;
  inputCode: string;
}